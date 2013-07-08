// --------------------------------------------------------------------------
// Copyright (C) 2012-2013 Best-Ever
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// --------------------------------------------------------------------------

package org.bestever.serverquery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.bestever.bebot.Bot;

public class QueryHandler extends Thread {

	/**
	 * The object to notify when we are done
	 */
	private QueryManager handlerThread;
	
	/**
	 * The requested IP/port to query
	 */
	private ServerQueryRequest request;
	
	/**
	 * A reference to the main bot
	 */
	private Bot bot;
	
	/**
	 * If we don't hear from the server in 5 seconds, then consider it dead/not working
	 */
	public static final int SOCKET_TIMEOUT_MS = 5000;
	
	/**
	 * This constructs the object so upon .run() it will gather the data and send it to the bot properly <br>
	 * Note that this does NOT invoke .run() automatically, the user must do so or else there
	 * will never be proper communication to the query manager about being done
	 * @param request The requested site
	 * @param handlerThread The query manager that we shall signal when we are done
	 */
	public QueryHandler(ServerQueryRequest request, Bot bot, QueryManager handlerThread) {
		this.request = request;
		this.handlerThread = handlerThread;
		this.bot = bot;
	}
	
	/**
	 * Handles the incoming packet we received
	 * @param data The data from the server
	 */
	private void processIncomingPacket(byte[] data) {
		NetworkBuffer networkBuffer = new NetworkBuffer(data.length);
		try {
			networkBuffer.add(data);
			QueryResult queryResult = new QueryResult();
			
			// Basic extractions we don't care about
			bot.sendMessageToChannel("Header: " + networkBuffer.extractInt(true)); // Header
			bot.sendMessageToChannel("Time: " + networkBuffer.extractInt(true)); // time
			bot.sendMessageToChannel("VersionString: " + networkBuffer.extractString()); // version
			bot.sendMessageToChannel("Flags: " + networkBuffer.extractInt(true)); //flags
			
			// Data extractions of what we want [needs more failsafes because of how it's structured]
			queryResult.setNumOfPwads(networkBuffer.extractByte());
			for (int i = 0; i < queryResult.getNumOfPwads(); i++)
				queryResult.pwad_names[i] = networkBuffer.extractString();
			queryResult.gamemode = networkBuffer.extractByte();
			queryResult.instagib = networkBuffer.extractByte();
			queryResult.buckshot = networkBuffer.extractByte();
			queryResult.iwad = networkBuffer.extractString();
			queryResult.skill = networkBuffer.extractByte();
			queryResult.dmflags = networkBuffer.extractInt(true); // Extract little endians for this
			queryResult.dmflags2 = networkBuffer.extractInt(true);
			queryResult.dmflags3 = networkBuffer.extractInt(true);
			queryResult.compatflags = networkBuffer.extractInt(true);
			queryResult.compatflags2 = networkBuffer.extractInt(true);
			bot.sendMessageToChannel("Extraction successful.");
		} catch (NetworkBufferException nbe) {
			nbe.printStackTrace();
			if (nbe.getMessage() != null)
				bot.sendMessageToChannel(nbe.getMessage());
			else
				bot.sendMessageToChannel("NetworkBufferException was thrown, please contact an administrator now.");
		} catch (Exception e) {
			e.printStackTrace();
			bot.sendMessageToChannel("Exception thrown, please contact an administrator now.");
		}
	}
	
	public void run() {
		// If any of these stay the same then something is wrong
		InetAddress IPAddress = null;
		int port = 0;
		byte[] dataToSend;
		byte[] dataToReceive = new byte[2048]; // Doubled standard size in case there's some dumb wad list with a lot of characters
		
		// Try with resources, we want to always have the socket close
		try (DatagramSocket connectionSocket = new DatagramSocket()) {
			// Setup our connection data
			IPAddress = InetAddress.getByName(request.getIP());
			port = request.getPort();
			
			// Put our data into a buffer and transform it before sending
			ByteBuffer byteBuffer = ByteBuffer.allocate(12);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			byteBuffer.putInt(ServerQueryFlags.LAUNCHER_CHALLENGE);
			byteBuffer.putInt(ServerQueryFlags.SQF_ALL_REQUEST_FLAGS);
			byteBuffer.putInt((int)System.currentTimeMillis()); // Zandro wiki specifies no unit for the time and is too ambigious, so I'll just send this
			byteBuffer.rewind();
			dataToSend = Huffman.encode(byteBuffer.array());
			
			// Now send the data
			DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, IPAddress, port);
			connectionSocket.send(sendPacket);
			
			// Block until we receive something or time out
			DatagramPacket receivePacket = new DatagramPacket(dataToReceive, dataToReceive.length);
			connectionSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
			connectionSocket.receive(receivePacket);
			
			// Prepare the data for processing
			byte[] receivedData = receivePacket.getData();
			byte[] receivedTruncatedData = new byte[receivePacket.getLength()];
			System.arraycopy(receivedData, 0, receivedTruncatedData, 0, receivePacket.getLength());
			byte[] decodedData = Huffman.decode(receivedTruncatedData);
			
			// Process it
			bot.sendMessageToChannel("Data received: " + Arrays.toString(decodedData));
			processIncomingPacket(decodedData);
			
		} catch (UnknownHostException e) {
			bot.sendMessageToChannel("IP of the host to query could not be determined. Please see if your IP is a valid address that can be reached.");
			e.printStackTrace();
		} catch (SocketException e) {
			bot.sendMessageToChannel("Error with the socket when handling query. Please try again or contact an administrator.");
			e.printStackTrace();
		} catch (SocketTimeoutException e) {
			bot.sendMessageToChannel("Socket timeout, IP is incorrect or server is down/unreachable (consider trying again if it is your first try).");
			e.printStackTrace();
		} catch (IOException e) {
			bot.sendMessageToChannel("IOException from query. Please try again or contact an administrator.");
			e.printStackTrace();
		} catch (Exception e) {
			bot.sendMessageToChannel("Unknown exception occured, contact an administrator now.");
			e.printStackTrace();
		}
		
		// Always alert the handler thread we are done
		handlerThread.signalProcessQueryComplete();
	}
}
