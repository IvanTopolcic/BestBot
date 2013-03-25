package org.bestever.bebot;

import java.io.BufferedWriter;
import java.io.File;
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
			serializationLine += "host_command[" + server.host_command + "];";
			serializationLine += "servername[" + server.servername + "];";
			serializationLine += "iwad[" + server.iwad + "];";
			serializationLine += "gamemode[" + server.host_command + "];";
			serializationLine += "rcon_password[" + server.rcon_password + "];";
			serializationLine += "play_time[" + server.play_time + "];";
			
			// Non essential
			if (server.config != null)
				serializationLine += "config[" + server.config + "];";
			if (server.wads != null)
				serializationLine += "wads[" + server.wads + "];";
			if (server.mapwads != null)
				serializationLine += "mapwads[" + server.mapwads + "];";
			if (server.server_parameters != null)
				serializationLine += "server_parameters[" + server.server_parameters + "];";
			if (server.disable_skulltag_data)
				serializationLine += "[" + Boolean.toString(server.disable_skulltag_data) + "];";
			if (server.instagib)
				serializationLine += "[" + Boolean.toString(server.instagib) + "];";
			if (server.buckshot)
				serializationLine += "[" + Boolean.toString(server.buckshot) + "];";
			if (server.dmflags > 0)
				serializationLine += "[" + Integer.toString(server.dmflags) + "];";
			if (server.dmflags2 > 0)
				serializationLine += "[" + Integer.toString(server.dmflags2) + "];";
			if (server.dmflags3 > 0)
				serializationLine += "[" + Integer.toString(server.dmflags3) + "];";
			if (server.compatflags > 0)
				serializationLine += "[" + Integer.toString(server.compatflags) + "];";
			if (server.compatflags2 > 0)
				serializationLine += "[" + Integer.toString(server.compatflags2) + "];";
			
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
	 */
	public static void unserializeServers(String serverFilePath, LinkedList<Server> serversToFillUp) {
		
	}
}