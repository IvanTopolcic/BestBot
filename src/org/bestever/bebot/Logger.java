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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Logger {
	
	public static String logfile = "";
	
	public static void setLogFile(String path) {
		Logger.logfile = path;
	}
	
	public static void logMessage(String message) {
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
		String time = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());
		
		// Append line to file
		pw.println(time + " " + message);
		
		// Close the stream
		pw.close();
	}
	
	public static void logMessage(String logfile, String message) {
		// Open file to prepare a print
		PrintWriter pw;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(logfile, true)));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// Get the current time
		String time = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());
		
		// Append line to file
		pw.println(time + " " + message);
		
		// Close the stream
		pw.close();
	}
}