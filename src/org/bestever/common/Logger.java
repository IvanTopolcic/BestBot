package org.bestever.common;

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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
	
	// Constants
	public static final int LOGLEVEL_CRITICAL = 0;
	public static final int LOGLEVEL_IMPORTANT = 10;
	public static final int LOGLEVEL_NORMAL = 30;
	public static final int LOGLEVEL_DEBUG = 70;
	public static final int LOGLEVEL_TRIVIAL = 100;
	
	// Logger variables
	public static String logfile = "";
	public static int log_level = LOGLEVEL_NORMAL;
	
	/**
	 * Sets up the path to the log file
	 * @param path A string containing the full path to the log file
	 */
	public static void setLogFile(String path) {
		Logger.logfile = path;
	}
	
	/**
	 * Writes a log message based on the log level
	 * @param logLevel int - severity of the message
	 * @param message String - the message to log
	 */
	public static void logMessage(int logLevel, String message) {
		// If it is not important enough for us then do not print it
		if (logLevel > log_level)
			return;
		
		// We should not have an empty logfile
		if (logfile == null || logfile.equals(""))
			return;
		
		// Open file to prepare a print
		PrintWriter pw;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(logfile, true)));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// Get the current time
		String time = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(Calendar.getInstance().getTime());
		
		// Append line to file
		pw.println(time + " " + message);
		
		// Close the stream
		pw.close();
	}
}