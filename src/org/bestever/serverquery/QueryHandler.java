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

/**
 * This is designed to handle a single process by messaging the server and then
 * waiting for a response; if a response is gotten it will then create the info
 * needed for the user, or state there was an error
 */
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
			int inboundFlags = networkBuffer.extractInt(true);
			
			if ((inboundFlags & ServerQueryFlags.SQF_NAME) == ServerQueryFlags.SQF_NAME)
				networkBuffer.extractString(); // Server name
			
			if ((inboundFlags & ServerQueryFlags.SQF_URL) == ServerQueryFlags.SQF_URL)
				networkBuffer.extractString(); // Server URL
			
			if ((inboundFlags & ServerQueryFlags.SQF_EMAIL) == ServerQueryFlags.SQF_EMAIL)
				networkBuffer.extractString(); // Server email
			
			if ((inboundFlags & ServerQueryFlags.SQF_MAPNAME) == ServerQueryFlags.SQF_MAPNAME)
				networkBuffer.extractString(); // Map name
			
			if ((inboundFlags & ServerQueryFlags.SQF_MAXCLIENTS) == ServerQueryFlags.SQF_MAXCLIENTS)
				networkBuffer.extractByte(); // Max clients allowed in the server (sv_maxclients)
			
			if ((inboundFlags & ServerQueryFlags.SQF_MAXPLAYERS) == ServerQueryFlags.SQF_MAXPLAYERS)
				networkBuffer.extractByte(); // Max players allowed in the server (sv_maxplayers)
			
			if ((inboundFlags & ServerQueryFlags.SQF_PWADS) == ServerQueryFlags.SQF_PWADS) {
				byte numOfPwads = networkBuffer.extractByte(); // How many loaded wads there are
				if (numOfPwads > 0) {
					String pwadList = "";
					for (int n = 0; n < numOfPwads; n++)
						if (n == numOfPwads - 1)
							pwadList += networkBuffer.extractString(); // Don't add a delimiter for the end of the list
						else
							pwadList += networkBuffer.extractString() + ",";
					queryResult.pwad_names = pwadList;
				}
			}
			
			if ((inboundFlags & ServerQueryFlags.SQF_GAMETYPE) == ServerQueryFlags.SQF_GAMETYPE) {
				queryResult.gamemode = networkBuffer.extractByte(); // Gamemode
				queryResult.instagib = networkBuffer.extractByte(); // Instagib
				queryResult.buckshot = networkBuffer.extractByte(); // Buckshot
			}
			
			if ((inboundFlags & ServerQueryFlags.SQF_GAMENAME) == ServerQueryFlags.SQF_GAMENAME)
				networkBuffer.extractString(); // Game base name (ex: DOOM, DOOM II, ...etc)
			
			if ((inboundFlags & ServerQueryFlags.SQF_IWAD) == ServerQueryFlags.SQF_IWAD)
				queryResult.iwad = networkBuffer.extractString(); // IWAD name
			
			if ((inboundFlags & ServerQueryFlags.SQF_FORCEPASSWORD) == ServerQueryFlags.SQF_FORCEPASSWORD)
				networkBuffer.extractByte(); // If a password is required
			
			if ((inboundFlags & ServerQueryFlags.SQF_FORCEJOINPASSWORD) == ServerQueryFlags.SQF_FORCEJOINPASSWORD)
				networkBuffer.extractByte(); // If a join password is required
			
			if ((inboundFlags & ServerQueryFlags.SQF_GAMESKILL) == ServerQueryFlags.SQF_GAMESKILL)
				queryResult.skill = networkBuffer.extractByte(); // Skill level
			
			if ((inboundFlags & ServerQueryFlags.SQF_BOTSKILL) == ServerQueryFlags.SQF_BOTSKILL)
				networkBuffer.extractByte(); // Bot skill level
			
			if ((inboundFlags & ServerQueryFlags.SQF_DMFLAGS) == ServerQueryFlags.SQF_DMFLAGS) {
				queryResult.dmflags = networkBuffer.extractInt(true); // dmflags
				queryResult.dmflags2 = networkBuffer.extractInt(true); // dmflags2
				queryResult.compatflags = networkBuffer.extractInt(true); // compatflags
			}
			
			if ((inboundFlags & ServerQueryFlags.SQF_LIMITS) == ServerQueryFlags.SQF_LIMITS) {
				networkBuffer.extractShort(true); // fraglimit
				networkBuffer.extractShort(true); // timelimit
				networkBuffer.extractShort(true); // time left (in minutes)
				networkBuffer.extractShort(true); // duellimit
				networkBuffer.extractShort(true); // pointlimit
				networkBuffer.extractShort(true); // winlimit
			}
			
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMDAMAGE) == ServerQueryFlags.SQF_TEAMDAMAGE)
				networkBuffer.extractInt(true); // This is a 32 bit float, no support right now
			
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMSCORES) == ServerQueryFlags.SQF_TEAMSCORES)
				networkBuffer.extractShort(true); // UNSURE: Claims deprecated, supposed to be the score for each team...
			
			byte numPlayers = 0;
			if ((inboundFlags & ServerQueryFlags.SQF_NUMPLAYERS) == ServerQueryFlags.SQF_NUMPLAYERS)
				numPlayers = networkBuffer.extractByte(); // Number of players in the server
			
			if ((inboundFlags & ServerQueryFlags.SQF_PLAYERDATA) == ServerQueryFlags.SQF_PLAYERDATA)
				for (int n = 0; n < numPlayers; n++) {
					networkBuffer.extractString(); // Player's name
					networkBuffer.extractShort(true); // Player's pointcount/fragcount/killcount
					networkBuffer.extractShort(true); // Player's ping
					networkBuffer.extractByte(); // Is spectator
					networkBuffer.extractByte(); // Is bot
					networkBuffer.extractByte(); // Player team (255 = no team)
					networkBuffer.extractByte(); // Player time in minutes in he server
				}
			
			byte numTeams = 0;
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMINFO_NUMBER) == ServerQueryFlags.SQF_TEAMINFO_NUMBER)
				numTeams = networkBuffer.extractByte(); // Number of teams
			
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMINFO_NAME) == ServerQueryFlags.SQF_TEAMINFO_NAME)
				for (int n = 0; n < numTeams; n++)
					networkBuffer.extractString(); // Team's name
			
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMINFO_COLOR) == ServerQueryFlags.SQF_TEAMINFO_COLOR)
				for (int n = 0; n < numTeams; n++)
					networkBuffer.extractInt(true); // Team's color
			
			if ((inboundFlags & ServerQueryFlags.SQF_TEAMINFO_SCORE) == ServerQueryFlags.SQF_TEAMINFO_SCORE)
				for (int n = 0; n < numTeams; n++)
					networkBuffer.extractShort(true); // Team's score
			
			if ((inboundFlags & ServerQueryFlags.SQF_TESTING_SERVER) == ServerQueryFlags.SQF_TESTING_SERVER) {
				networkBuffer.extractByte(); // True/false if using a custom binary
				networkBuffer.extractString(); // Empty string if stable binary, testing binary name otherwise
			}
			
			if ((inboundFlags & ServerQueryFlags.SQF_DATA_MD5SUM) == ServerQueryFlags.SQF_DATA_MD5SUM)
				networkBuffer.extractString(); // MD5 sum
			
			if ((inboundFlags & ServerQueryFlags.SQF_ALL_DMFLAGS) == ServerQueryFlags.SQF_ALL_DMFLAGS) {
				int numOfFlags = networkBuffer.extractByte();
				if (numOfFlags > 0)
					queryResult.dmflags = networkBuffer.extractInt(true); // dmflags
				if (numOfFlags > 1)
					queryResult.dmflags2 = networkBuffer.extractInt(true); // dmflags2
				if (numOfFlags > 2)
					queryResult.dmflags3 = networkBuffer.extractInt(true); // dmflags3
				if (numOfFlags > 3)
					queryResult.compatflags = networkBuffer.extractInt(true); // compatflags
				if (numOfFlags > 4)
					queryResult.compatflags2 = networkBuffer.extractInt(true); // compatflags2
			}
				
			if ((inboundFlags & ServerQueryFlags.SQF_SECURITY_SETTINGS) == ServerQueryFlags.SQF_SECURITY_SETTINGS)
				networkBuffer.extractByte(); // If enforcing the master
			
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
		String queryOutput = ".host";
		
		if (queryResult.pwad_names != null)
			queryOutput += " wads=" + queryResult.pwad_names;
			
		if (queryResult.gamemode != -1)
			queryOutput += " gamemode=" + ServerQueryFlags.getGamemodeFromFlag(queryResult.gamemode);
			
		if (queryResult.instagib != -1)
			if (queryResult.instagib == 0)
				queryOutput += " instagib=off";
			else
				queryOutput += " instagib=on";
			
		if (queryResult.buckshot != -1)
			if (queryResult.buckshot == 0)
				queryOutput += " buckshot=off";
			else
				queryOutput += " buckshot=on";
			
		if (queryResult.iwad != null)
			queryOutput += " iwad=" + queryResult.iwad;
			
		if (queryResult.skill != -1)
			queryOutput += " skill=" + queryResult.skill;
			
		if (queryResult.dmflags != -1)
			queryOutput += " dmflags=" + queryResult.dmflags;
			
		if (queryResult.dmflags2 != -1)
			queryOutput += " dmflags2=" + queryResult.dmflags2;
			
		if (queryResult.dmflags3 != -1)
			queryOutput += " dmflags3=" + queryResult.dmflags3;
			
		if (queryResult.compatflags != -1)
			queryOutput += " compatflags=" + queryResult.compatflags;
		
		if (queryResult.compatflags2 != -1)
			queryOutput += " compatflags2=" + queryResult.compatflags2;
		
		bot.sendMessageToChannel("Query complete: " + queryOutput);
	}
	
	public void run() {
		// If any of these stay the same then something is wrong
		InetAddress IPAddress;
		try {
			IPAddress = InetAddress.getByName(request.getIP());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			bot.sendMessageToChannel("Error: Query IP address could not be resolved or is using IPv6.");
			return;
		}
		int port = request.getPort();
		byte[] dataToSend = null;
		byte[] dataToReceive = new byte[2048]; // Doubled standard size in case there's some dumb wad list with a lot of characters
		
		// Try with resources, we want to always have the socket close
		try (DatagramSocket connectionSocket = new DatagramSocket()) {		
			// Prepare our send packet		
			dataToSend = new byte[] { (byte)199, 0, 0, 0, -64, 18, 0, 8 }; // Send challenge and then SQF_FlagsStuff
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
