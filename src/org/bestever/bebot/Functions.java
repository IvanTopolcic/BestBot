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

package org.bestever.bebot;

import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Functions {
	
	/**
	 * If no port is available, this is returned
	 */
	public static final int NO_AVAILABLE_PORT = 0;

	/**
	 * Generates an MD5 hash
	 * @return 32 character MD5 hex string
	 */
	public static String generateHash() throws NoSuchAlgorithmException {
		String seed = System.nanoTime() + "SOON";
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(seed.getBytes());
		return byteArrayToHex(md.digest());
	}

	/**
	 * Returns an array of lines from character
	 * Ignores simple C-style comments (//)
	 * @param file full path to file
	 * @return String[] of lines
	 * @throws IOException
	 */
	public static String[] readFile(String file) throws IOException {
		if (!fileExists(file)) {
			Logger.logMessage(Logger.LOGLEVEL_IMPORTANT, "Could not open file '" + file + "' for reading.");
			return null;
		}
		ArrayList<String> lines = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		while (br.readLine() != null) {
			if (!br.readLine().startsWith("//")) {
				lines.add(br.readLine());
			}
		}
		br.close();
		return lines.toArray(new String[lines.size()]);
	}

	/**
	 * Given a byte array, returns a hexadecimal string
	 * @param bytes byte array
	 * @return 16bit hex string
	 */
	public static String byteArrayToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes)
			sb.append(String.format("%02x", b&0xff));
		return sb.toString();
	}
	
	/**
	 * Gets the name of a user by splitting their hostname (*.users.zandronum.com)
	 * @param hostname The user's host name
	 * @return username The user's actual IRC name
	 */
	public static String getUserName(String hostname) {
		return hostname.replace(".users.zandronum.com", "");
	}
	
	/**
	 * Checks to see if a user is logged on their Zandronum IRC account
	 * @param hostname The user's hostname
	 * @return username True if logged in, false if not
	 */
	public static boolean checkLoggedIn(String hostname) {
		hostname = hostname.replace(".users.zandronum.com", "");
		return !hostname.contains(".");
	}
	
	/**
	 * Checks to see if a number is numeric
	 * In a recent update, now checks safely for nulls should such a thing happen
	 * @param maybeid The String to check (does parse double)
	 * @return True if it is a number, false if it's not
	 */
	public static boolean isNumeric(String maybeid) {
		try {
			Double.parseDouble(maybeid);
		} catch (NumberFormatException nfe)	{
			return false;
		} catch (NullPointerException npe) {
			return false;
		}
		return true;
	}
	
	/**
	 * Checks to see if a given port is in use
	 * @param checkport The port to check
	 * @return True if it's available, false if not
	 */
	public static boolean checkIfPortAvailable(int checkport) {
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(checkport);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(checkport);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
			//e.printStackTrace();
		} finally {
			if (ds != null)
				ds.close();
			if (ss != null)
				try {
					ss.close();
				} catch (IOException e) { }
		}
		return false;
	}
	
	/**
	 * Checks for an available port from minport up to (but NOT including) maxport
	 * @param minport The minimum port to check
	 * @param maxport The maximum port that is one above what you would check (ex: 20200 would be the same as checking up to 20199)
	 * @return The first available port, or 0 if no port is available
	 */
	public static int getFirstAvailablePort(int minport, int maxport) {
		for (int p = minport; p < maxport; p++) {
			if (checkIfPortAvailable(p))
				return p;
		}
		return 0;
	}
	
	/**
	 * Function that takes a time in seconds
	 * and converts it to a string with days, hours, minutes
	 * and seconds.
	 * @param nanoSeconds in long format
	 * @return A String in a readable format
	 */
	public static String calculateTime(long nanoSeconds) {
		nanoSeconds = nanoSeconds / 1000000000;
		int day = (int)TimeUnit.SECONDS.toDays(nanoSeconds);
		long hours = TimeUnit.SECONDS.toHours(nanoSeconds) - (day *24);
		long minute = TimeUnit.SECONDS.toMinutes(nanoSeconds) - (TimeUnit.SECONDS.toHours(nanoSeconds)* 60);
		long second = TimeUnit.SECONDS.toSeconds(nanoSeconds) - (TimeUnit.SECONDS.toMinutes(nanoSeconds) *60);
		return day + " days " + hours + " hours " + minute + " minutes and " + second + " seconds.";
	}

	/**
	 * Takes a comma-seperated list of wads and returns a string parseable by Zandronum
	 * @param wads comma-seperated list of wads
	 * @return zandronum-parseable wad list
	 */
	public static String parseWads (String wads, String directory) {
		String wadArray[] = wads.split(",");
		for (int i = 0; i < wadArray.length; i++)
			wadArray[i] = directory + wadArray[i].trim();
		return implode(wadArray, " ");
	}

	/**
	 * Check if a file exists
	 * @param file String path to file
	 * @return true if exists, false if not
	 */
	public static boolean fileExists(String file) {
		File f = new File(file);
		if (f.exists())
			return true;
		else
			return false;
	}

	/**
	 * Returns a cleaned string for file inputs
	 * @param input
	 * @return cleaned string
	 */
	public static String cleanInputFile(String input) {
		System.out.println(input);
		System.out.println(input.trim());
		System.out.println(input.replace(" ",""));
		return input.replace("/", "").trim();
	}

	/**
	 * Implodes a character between a string array
	 * @param inputArray
	 * @param glueString
	 * @return String containing all array elements seperated by glue string
	 */
	public static String implode(String[] inputArray, String glueString) {
		String output = "";
		if (inputArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(inputArray[0]);
			for (int i = 1; i < inputArray.length; i++) {
				sb.append(glueString);
				sb.append(inputArray[i].trim());
			}
			output = sb.toString();
		}
		return output;
	}


	/**
	 * Giving money is nice!
	 * @return A string with a serious message
	 */
	public static String giveMeMoney() {
		return giveMeMoneyLines[(int)(Math.random() * giveMeMoneyLines.length)];
	}
	
	// Yes ^_^
	public static final String[] giveMeMoneyLines = {
		"Yes", "No", "Nope", "Of course", "Why?", "Maybe...", "Perhaps", "Maybe", "Probably not", "Not likely", "Soon", "Never", "Stop asking", "Chances are low",
		"Definitely not", "Never ever", "Hmm... no", "Pass", "Nah", "Improbable", "Unlikely"
	};
	
}
