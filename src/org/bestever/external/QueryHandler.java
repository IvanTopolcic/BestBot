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

package org.bestever.external;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
	 * If we don't hear from the server in 10 seconds, then consider it dead/not working
	 */
	public static final int SOCKET_TIMEOUT_MS = 10000;
	
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
	
	private void processIncomingPacket(byte[] data, int length) {
		//byte string; byte byte byte; string; byte byte int int int int int
		bot.sendMessageToChannel("Query done.");
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
			ByteBuffer byteBuffer = ByteBuffer.allocate(8);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			byteBuffer.putInt(ServerQueryFlags.LAUNCHER_CHALLENGE);
			byteBuffer.putInt(ServerQueryFlags.SQF_ALL_REQUEST_FLAGS);
			byteBuffer.rewind();
			dataToSend = Huffman.encode(byteBuffer.array());
			
			// Now send the data
			DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, IPAddress, port);
			connectionSocket.send(sendPacket);
			
			// Block until we receive something or time out
			DatagramPacket receivePacket = new DatagramPacket(dataToReceive, dataToReceive.length);
			connectionSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
			connectionSocket.receive(receivePacket);
			processIncomingPacket(Huffman.decode(receivePacket.getData()), receivePacket.getLength()); // Decoding done here
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
		}
		
		// Always alert the handler thread we are done
		handlerThread.signalProcessQueryComplete();
	}
}
