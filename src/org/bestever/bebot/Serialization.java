package org.bestever.bebot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

public class Serialization {

	/**
	 * This will serialize the servers currently run by the bot. It will save them in a text
	 * file that will be parsed.
	 * @param pathToWriteTo The path to write the file to
	 * @param servers The servers to write
	 */
	public static void serializeServers(String pathToWriteTo, LinkedList<Server> servers) {
		// Don't write to a messed up path
		if (pathToWriteTo == null) {
			System.out.println("Invalid serialization path.");
			return;
		}
		
		// Don't do anything if there's an error
		if (servers == null) {
			System.out.println("Can't serialize a null server.");
			return;
		}
		
		// If we have no servers, don't bother
		if (servers.size() <= 0) {
			System.out.println("No servers to write.");
			return;
		}
		
		// Open the file and prepare to write
		File serverFile = new File(pathToWriteTo);
		
		// If it exists, delete it
		if (serverFile.exists()) {
			if (!serverFile.delete()) {
				System.out.println("Error deleting file. Not serializing onto a full file");
				return;
			}
		}
		
		// Now that it is deleted, create a new file to write to
		try {
			if (!serverFile.createNewFile()) {
				System.out.println("Error creating serialization file.");
				return;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(serverFile));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// Go through each server and write it to the file
		ListIterator<Server> it = servers.listIterator();
		Server server;
		String serializationLine;
		while (it.hasNext()) {
			server = it.next();
			serializationLine = "";
			
			// Mandatory fields that must be added
			serializationLine += "time_started[" + server.time_started + "];";
			serializationLine += "sender[" + server.sender + "];";
			serializationLine += "irc_channel[" + server.irc_channel + "];";
			serializationLine += "irc_hostname[" + server.irc_hostname + "];";
			serializationLine += "irc_login[" + server.irc_login + "];";
			serializationLine += "iwad[" + server.iwad + "];";
			serializationLine += "gamemode[" + server.gamemode + "];";
			serializationLine += "rcon_password[" + server.rcon_password + "];";
			serializationLine += "server_id[" + server.server_id + "];";
			serializationLine += "play_time[" + server.play_time + "];";
			
			// Non essential
			if (server.config != null)
				serializationLine += "config[" + server.config + "];";
			if (server.wads != null)
				serializationLine += "wads[" + server.wads + "];";
			if (server.mapwads != null)
				serializationLine += "mapwads[" + server.mapwads + "];";
			if (server.disable_skulltag_data)
				serializationLine += "disable_skulltag_data[" + Boolean.toString(server.disable_skulltag_data) + "];";
			if (server.instagib)
				serializationLine += "instagib[" + Boolean.toString(server.instagib) + "];";
			if (server.buckshot)
				serializationLine += "buckshot[" + Boolean.toString(server.buckshot) + "];";
			if (server.dmflags > 0)
				serializationLine += "dmflags[" + Integer.toString(server.dmflags) + "];";
			if (server.dmflags2 > 0)
				serializationLine += "dmflags2[" + Integer.toString(server.dmflags2) + "];";
			if (server.dmflags3 > 0)
				serializationLine += "dmflags3[" + Integer.toString(server.dmflags3) + "];";
			if (server.compatflags > 0)
				serializationLine += "compatflags[" + Integer.toString(server.compatflags) + "];";
			if (server.compatflags2 > 0)
				serializationLine += "compatflags2[" + Integer.toString(server.compatflags2) + "];";
			
			// Leave this for the end since it's irritating to process
			serializationLine += "servername[" + server.servername + "];";
			
			// Always end with starting a new line (one server per line)
			serializationLine += "\n";
			
			// Try writing, if something goes wrong 
			try {
				writer.write(serializationLine);
			} catch (IOException e) {
				try {
					writer.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				return;
			}
		}
		
		// Close at the end
		try {
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * This deserializes the servers and handles/runs them.
	 * @param serverFilePath The text file path the data was saved to
	 * @param serversToFillUp The reference to the variable we will write to
	 * @throws IOException If something goes wrong
	 */
	public static void unserializeServers(String serverFilePath, LinkedList<Server> serversToFillUp) throws IOException {
		// Ensure we are pointed to a file
		if (serverFilePath == null) {
			System.out.println("Must have a valid server file path");
			return;
		}
		
		// Open a reader
		BufferedReader reader = new BufferedReader(new FileReader(new File(serverFilePath)));
		
		// There will be one server per line
		Server server;
		String serverData = "";
		while ((serverData = reader.readLine()) != null) {
			server = new Server();
			
			server.time_started = getSerializedLong(serverData, "time_started[", "];");
			server.sender = getSerializedString(serverData, "sender[", "];");
			server.irc_channel = getSerializedString(serverData, "irc_channel[", "];");
			server.irc_hostname = getSerializedString(serverData, "irc_hostname[", "];");
			server.irc_login = getSerializedString(serverData, "irc_login[", "];");
			server.iwad = getSerializedString(serverData, "iwad[", "];");
			server.gamemode = getSerializedString(serverData, "gamemode[", "];");
			server.rcon_password = getSerializedString(serverData, "rcon_password[", "];");
			server.server_id = getSerializedString(serverData, "server_id[", "];");
			server.play_time = getSerializedLong(serverData, "play_time[", "];");
			server.servername = getSerializedHostname(serverData, "servername[", "];"); // Must be at the end of the string
			
			// Ensure there was no errors, if so then just continue on without adding it
			if (server.validServer()) {
				
				// Check for possible parameters
			
				// At the end, add it to the linked list, and then run it
				serversToFillUp.add(server);
				server.serverprocess = new ServerProcess(server);
				server.serverprocess.start();
			}
		}
		
		// Close when done
		reader.close();
	}
	
	/**
	 * This specifically grabs the hostname at the end of the list
	 * @param serverData The line for the server
	 * @param beginning The first part 
	 * @param veryEnd The very end of the line which we should read between
	 * @return The hostname, or null if something went wrong
	 */
	public static String getSerializedHostname(String serverData, String beginning, String veryEnd) {
		return null;
	}

	/**
	 * Gets a serialized String from the requested line
	 * @param line The line to process
	 * @param beginning The beginning marker
	 * @param end The end marker
	 * @return A String of what we want; null if there's an exception
	 */	
	public static String getSerializedString(String line, String beginning, String end) {
		try {
			int startIndex = line.indexOf(beginning) + beginning.length();
			int endIndex = line.indexOf(end, startIndex) + end.length();
			return line.substring(startIndex, endIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Grabs the data between beginning and end as a long.
	 * @param line The line to process
	 * @param beginning The beginning marker
	 * @param end The end marker
	 * @return A long value of what we want; 0 if there's an exception
	 */
	public static long getSerializedLong(String line, String beginning, String end) {
		try {
			String numberString = getSerializedString(line, beginning, end);
			Long.parseLong(numberString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * Gets a serialized integer from the requested line
	 * @param line The line to process
	 * @param beginning The beginning marker
	 * @param end The end marker
	 * @return A int value of what we want; 0 if there's an exception
	 */
	public static int getSerializedInt(String line, String beginning, String end) {
		try {
			String numberString = getSerializedString(line, beginning, end);
			Integer.parseInt(numberString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}