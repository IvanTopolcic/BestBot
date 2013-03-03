package org.bestever.bebot;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class Functions {
	
	/**
	 * Checks the array to see if a string is found inside of it
	 * @param array The array of strings (if null or index < 0 returns false)
	 * @param s The string to check (case sensitive)
	 * @return True if found, false if not/or error
	 */
	public static boolean stringArrayContains(String[] array, String s) {
		// Safety check
		if (array == null || array.length < 1)
			return false;
		
		// For each element, check if it equals
		for (String index : array)
			if (index.equals("#" + s))
				return true;
			
		// If nothing, return false
		return false;
	}
	
	/**
	 * Generates a unique ID
	 * Unique ID is a 12 character MD5 hash
	 * @return A string containing the uniqueID
	 */
	public static String getUniqueID(String banlist_directory)
	{
		String temp = System.nanoTime()+"";
		String ID = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(temp.getBytes());
			BigInteger hash = new BigInteger(1, md.digest());
			ID = hash.toString(16);
			ID = ID.substring(0, Math.min(ID.length(), 12));
			File f = new File(banlist_directory + ID + ".txt");
			while (f.exists()) {
				temp = System.nanoTime()+"";
				md.update(temp.getBytes());
				hash = new BigInteger(1, md.digest());
				ID = hash.toString(16);
				ID = ID.substring(0, Math.min(ID.length(), 10));
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
	public static String getUserName(String hostname)
	{
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
	public static boolean isNumeric(String maybeid)
	{
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
	 * Function that takes a time in seconds
	 * and converts it to a string with days, hours, minutes
	 * and seconds.
	 * @param Seconds in long format
	 * @return A String in a readable format
	 */
	public static String calculateTime(long seconds)
	{
		int day = (int)TimeUnit.SECONDS.toDays(seconds);        
		long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
		long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
		long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
		return day + " days " + hours + " hours " + minute + " minutes and " + second + " seconds.";
	}
}
