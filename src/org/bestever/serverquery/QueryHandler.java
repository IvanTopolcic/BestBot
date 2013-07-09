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

import org.bestever.bebot.Bot;
import org.bestever.bebot.Utility;

public class QueryHandler extends Thread {

	/**
	 * The object to notify when we are done
	 */
	private QueryManager handlerThread;
	
	/**
	 * The requested IP/port to query
	 */
	@SuppressWarnings("unused") // WHY JAVA
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
			int header = networkBuffer.extractInt(true);
			if (header == 5660024)
				throw new NetworkPacketProcessException("Querying server too much.");
			else if (header == 5660025)
				throw new NetworkPacketProcessException("Host has banned the IP.");
			
			// Extract time, as it's useles to us right now
			networkBuffer.extractInt(true); 
			
			// Extract version string, as it's useles to us right now
			networkBuffer.extractString(); 
			
			// What the server wants to send back to us (and read every flag safely)
			int inboundFlags = Utility.flipEndianInt(networkBuffer.extractInt(true));
			
			if ((inboundFlags & ServerQueryFlags.SQF_NAME) == ServerQueryFlags.SQF_NAME)
				networkBuffer.extractString(); // Server name
			
			if ((inboundFlags & ServerQueryFlags.SQF_URL) == ServerQueryFlags.SQF_URL)
				networkBuffer.extractString(); // Server URL
			
			if ((inboundFlags & ServerQueryFlags.SQF_EMAIL) == ServerQueryFlags.SQF_EMAIL)
				networkBuffer.extractString(); // Server email
			
			if ((inboundFlags & ServerQueryFlags.SQF_MAPNAME) == ServerQueryFlags.SQF_MAPNAME)
				networkBuffer.extractString(); // Map name
			
			if ((inboundFlags & ServerQueryFlags.SQF_MAXCLIENTS) == ServerQueryFlags.SQF_MAXCLIENTS)
				networkBuffer.extractByte(); // Max clients
			
			if ((inboundFlags & ServerQueryFlags.SQF_MAXPLAYERS) == ServerQueryFlags.SQF_MAXPLAYERS)
				networkBuffer.extractByte();
			
			if ((inboundFlags & ServerQueryFlags.SQF_PWADS) == ServerQueryFlags.SQF_PWADS) {
				byte numOfPwads = networkBuffer.extractByte();
				if (numOfPwads > 0) {
					String pwadList = "";
					for (int i = 0; i < numOfPwads; i++)
						if (i == numOfPwads - 1)
							pwadList += networkBuffer.extractString(); // Don't add a delimiter for the end of the list
						else
							pwadList += networkBuffer.extractString() + ",";
					queryResult.pwad_names = pwadList;
				}
			}
			
			if ((inboundFlags & ServerQueryFlags.SQF_GAMETYPE) == ServerQueryFlags.SQF_GAMETYPE) {
				networkBuffer.extractByte();
				networkBuffer.extractByte();
				networkBuffer.extractByte();
			}
			if ((inboundFlags & ServerQueryFlags.SQF_GAMENAME) == ServerQueryFlags.SQF_GAMENAME)
				networkBuffer.extractString();
			
			if ((inboundFlags & ServerQueryFlags.SQF_IWAD) == ServerQueryFlags.SQF_IWAD)
				networkBuffer.extractString();
			
			if ((inboundFlags & ServerQueryFlags.SQF_FORCEPASSWORD) == ServerQueryFlags.SQF_FORCEPASSWORD)
				networkBuffer.extractByte();
			
			if ((inboundFlags & ServerQueryFlags.SQF_FORCEJOINPASSWORD) == ServerQueryFlags.SQF_FORCEJOINPASSWORD)
				networkBuffer.extractByte();
			
			if ((inboundFlags & ServerQueryFlags.SQF_GAMESKILL) == ServerQueryFlags.SQF_GAMESKILL)
				networkBuffer.extractByte();
			
			if ((inboundFlags & ServerQueryFlags.SQF_BOTSKILL) == ServerQueryFlags.SQF_BOTSKILL)
				networkBuffer.extractByte();
			
			if ((inboundFlags & ServerQueryFlags.SQF_DMFLAGS) == ServerQueryFlags.SQF_DMFLAGS) {
				networkBuffer.extractInt(true);
				networkBuffer.extractInt(true);
				networkBuffer.extractInt(true);
			}
			
			if ((inboundFlags & ServerQueryFlags.SQF_LIMITS) == ServerQueryFlags.SQF_LIMITS) {
				networkBuffer.extractShort(true);
				networkBuffer.extractShort(true);
				networkBuffer.extractShort(true);
				networkBuffer.extractShort(true);
				networkBuffer.extractShort(true);
				networkBuffer.extractShort(true);
			}
			
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMDAMAGE) == ServerQueryFlags.SQF_TEAMDAMAGE)
				networkBuffer.extractInt(true); // This is a 32 bit float, no support right now
			
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMSCORES) == ServerQueryFlags.SQF_TEAMSCORES)
				networkBuffer.extractShort(true);
			
			byte numPlayers = 0;
			if ((inboundFlags & ServerQueryFlags.SQF_NUMPLAYERS) == ServerQueryFlags.SQF_NUMPLAYERS)
				numPlayers = networkBuffer.extractByte();
			
			if ((inboundFlags & ServerQueryFlags.SQF_PLAYERDATA) == ServerQueryFlags.SQF_PLAYERDATA)
				for (int i = 0; i < numPlayers; i ++) {
					networkBuffer.extractString();
					networkBuffer.extractShort(true);
					networkBuffer.extractShort(true);
					networkBuffer.extractByte();
					networkBuffer.extractByte();
					networkBuffer.extractByte();
					networkBuffer.extractByte();
				}
			
			byte numTeams = 0;
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMINFO_NUMBER) == ServerQueryFlags.SQF_TEAMINFO_NUMBER)
				numTeams = networkBuffer.extractByte();
			
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMINFO_NAME) == ServerQueryFlags.SQF_TEAMINFO_NAME)
				for (int i = 0; i < numTeams; i++)
					networkBuffer.extractString();
			
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMINFO_COLOR) == ServerQueryFlags.SQF_TEAMINFO_COLOR)
				for (int i = 0; i < numTeams; i++)
					networkBuffer.extractInt(true);
			
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMINFO_SCORE) == ServerQueryFlags.SQF_TEAMINFO_SCORE)
				for (int i = 0; i < numTeams; i++)
					networkBuffer.extractShort(true);
			
			if ((inboundFlags & ServerQueryFlags.SQF_TESTING_SERVER) == ServerQueryFlags.SQF_TESTING_SERVER)
				networkBuffer.extractString();
			
			if ((inboundFlags & ServerQueryFlags.SQF_DATA_MD5SUM) == ServerQueryFlags.SQF_DATA_MD5SUM)
				networkBuffer.extractString();
			
			if ((inboundFlags & ServerQueryFlags.SQF_ALL_DMFLAGS) == ServerQueryFlags.SQF_ALL_DMFLAGS) {
				int numOfFlags = networkBuffer.extractByte();
				if (numOfFlags > 0)
					networkBuffer.extractInt(true);
				if (numOfFlags > 1)
					networkBuffer.extractInt(true);
				if (numOfFlags > 2)
					networkBuffer.extractInt(true);
				if (numOfFlags > 3)
					networkBuffer.extractInt(true);
				if (numOfFlags > 4)
					networkBuffer.extractInt(true);
			}
				
			if ((inboundFlags & ServerQueryFlags.SQF_SECURITY_SETTINGS) == ServerQueryFlags.SQF_SECURITY_SETTINGS)
				networkBuffer.extractByte();
			
			// Display the final result in the channel
			displayQueryResult(queryResult);
		} catch (NetworkBufferException nbe) {
			nbe.printStackTrace();
			if (nbe.getMessage() != null)
				bot.sendMessageToChannel(nbe.getMessage());
			else
				bot.sendMessageToChannel("NetworkBufferException was thrown, please contact an administrator now.");
		} catch (NetworkPacketProcessException nppe) {
			nppe.printStackTrace();
			bot.sendMessageToChannel("Network exception: " + nppe.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			bot.sendMessageToChannel("Exception thrown, please contact an administrator now.");
		}
	}
	
	public void displayQueryResult(QueryResult queryResult) {
		System.out.println(queryResult.dmflags);
	}
	
	public void run() {
		// If any of these stay the same then something is wrong
		InetAddress IPAddress = null;
		int port = 0;
		byte[] dataToSend = null;
		byte[] dataToReceive = new byte[2048]; // Doubled standard size in case there's some dumb wad list with a lot of characters
		
		// Try with resources, we want to always have the socket close
		try (DatagramSocket connectionSocket = new DatagramSocket()) {		
			// Prepare our send packet		
			dataToSend = new byte[] { (byte)199, 0, 0, 0, 1, 0, 0, 0 }; // Send challenge and then SQF_Flags
			byte[] huffmanToSend = Huffman.encode(dataToSend);
			
			// Now send the data
			DatagramPacket sendPacket = new DatagramPacket(huffmanToSend, huffmanToSend.length, IPAddress, port);
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
