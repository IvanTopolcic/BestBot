package org.bestever.common;

//--------------------------------------------------------------------------
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
//--------------------------------------------------------------------------

import org.bestever.irc.BotIRC;
import org.bestever.server.BotHoster;

public class Core {

	/**
	 * Displays runtime help.
	 */
	private static void displayHelp() {
		System.out.println("Bestbot usage: java bestbot.jar <-irc | -host> <ini_path.ini>");
		System.out.println("\t-host\t\tWill host servers.");
		System.out.println("\t-irc\t\tRuns the IRC bot.");
		System.out.println("\t<ini file>\tThe ini file to load.");
		System.out.println("\tExample: java -jar bestbot.jar -irc bestbotini.ini");
	}
	
	/**
	 * Program execution from here.
	 * @param args The runtime args.
	 */
	public static void main(String[] args) {
		boolean isServerHoster = false;
		boolean isIRCBot = false;
		String iniName = null;
		
		// Go through each argument
		String lowerArg;
		for (String s : args) {
			lowerArg = s.toLowerCase();
			
			// If we want to run the IRC bot from this machine
			if (lowerArg.equals("-irc")) {
				isIRCBot = true;
				continue;
			}
			
			// If we want to host servers from this machine
			if (lowerArg.equals("-host")) {
				isServerHoster = true;
				continue;
			}
			
			// If we supplied an ini file
			if (lowerArg.endsWith(".ini")) {
				iniName = s;
				continue;
			}
		}
		
		/*
		// We shouldn't have server and irc together
		if (isServerHoster && isIRCBot) {
			System.out.println("You specified both -irc and -host, please select one and re-run the bot.");
			displayHelp();
			return;
		}
		
		// We should have only one of the arguments set (finally, I get to use XOR for once in my life)
		if (!(isServerHoster ^ isIRCBot)) {
			System.out.println("You need to specify either -irc or -host in your command line. Please choose one and re-run the bot.");
			displayHelp();
			return;
		}
		*/
		
		// There needs to always be an .ini file
		if (iniName == null) {
			System.out.println("There must be a proper ini file.");
			displayHelp();
			return;
		}
		
		// Initialize the proper file
		if (isServerHoster)
			new Thread(new BotHoster(iniName), "BotHoster").start();
		if (isIRCBot)
			new Thread(new BotIRC(iniName), "IRCBot").start();
	}
}
