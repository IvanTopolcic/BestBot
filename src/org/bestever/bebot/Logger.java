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