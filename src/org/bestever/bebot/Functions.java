package org.bestever.bebot;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class Functions {
	
	/**
	 * If no port is available, this is returned
	 */
	public static final int NO_AVAILABLE_PORT = 0;
	
	/**
	 * Generates a unique ID
	 * Updated to remove file creation
	 * Unique ID is a 12 character MD5 hash
	 * @param banlist_directory The directory to check for clashing ID's
	 * @return A string containing the uniqueID
	 */
	public static String getUniqueID(String banlist_directory) {
		String temp = Long.toString(System.nanoTime());
		String ID = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(temp.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			ID = hash.toString(16);
			ID = ID.substring(0, Math.min(ID.length(), 12));
			File f = new File(banlist_directory + ID + ".txt");
			while (f.exists()) {
				temp = Long.toString(System.nanoTime());
				md.update(temp.getBytes());
				hash = new BigInteger(1, md.digest());
				ID = hash.toString(16);
				f = new File(banlist_directory + ID + ".txt");
			}
		} catch (NoSuchAlgorithmException e1) {
			System.out.println("Error generating unique ID.");
			ID = "None";
		}
		return ID;
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
				} catch (IOException e) {
					//e.printStackTrace();
				}
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
	 * @param seconds in long format
	 * @return A String in a readable format
	 */
	public static String calculateTime(long seconds) {
		int day = (int)TimeUnit.SECONDS.toDays(seconds);        
		long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
		long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
		long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
		return day + " days " + hours + " hours " + minute + " minutes and " + second + " seconds.";
	}
}
