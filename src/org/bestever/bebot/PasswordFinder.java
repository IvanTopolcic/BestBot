package org.bestever.bebot;

import static org.bestever.bebot.Logger.logMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Threaded class that parses logfiles and finds admin information
 */
public class PasswordFinder
{	
	
	/**
	 * This method is called when information about the server is requested
	 * It returns two string, the latest RCON password and the Unique ID
	 * @param port
	 * @return RCON password, Unique ID
	 */
	public String[] getServerInformation(String port) {
		// Get the <unique_id> using port somehow.. finish this when we have hosting working
		File logfile = new File(getServerUniqueID(port));
		return new String[]{ getRconPassword(logfile), getServerUniqueID(port) };
		
	}
	/**
	 * Method to get the unique ID of the server
	 */
	public String getServerUniqueID(String port) {
		// Filler because we cannot get the Unique ID yet
		String id = "";
		return id;
	}
	
	/**
	 * Method to parse the logfile bottom up and check for RCON password
	 * @param logfile
	 * @return RCON
	 */
	public static String getRconPassword(File logfile) {
		// Holy shit Java 7 try with resources at work!
		try (BufferedReader in = new BufferedReader(new InputStreamReader (new ReverseLineInputStream(logfile)))) {
			// ReverseLineInputStream is a custom class that takes advantage of RadomAccessFile
			// to read the file line by line from the bottom up instead of storing the entire
			// thing into memory
			
			while (in.readLine() != null) {
				String line = in.readLine();
				if (line.toLowerCase().contains("-> sv_rconpassword") || line.toLowerCase().contains("\"sv_rconpassword\" is \"")) {
					// We've found the password!
					in.close();
					// Parse the RCON password
					String[] parseLine = line.split(" ");
					return parseLine[4].replace("\"","");
				}
			}
			in.close();
		} catch (IOException e) {
			System.out.println("ERROR: GENERAL_ERROR in 'getRconPassword()'");
			logMessage("ERROR: GENERAL_ERROR in 'getRconPassword()'");
			e.printStackTrace();
		}
		return "Could not find RCON password.";
	}
}