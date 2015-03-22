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
import java.net.URLEncoder;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class is specifically for running the server only and notifying the
 * bot when the server is closed, or when to be terminated; nothing more
 */
public class ServerProcess extends Thread {

	/**
	 * This contains the strings that will run in the process builder
	 */
	private ArrayList<String> serverRunCommands;

	/**
	 * A reference to the server
	 */
	private Server server;

	/**
	 * The process of the server
	 */
	private Process proc;

	/**
	 * Used in determining when the last activity of the server was in ms
	 */
	public long last_activity;

	/**
	 * This should be called before starting run
	 * @param serverReference A reference to the server it is connected to (establishing a back/forth relationship to access its data)
	 */
	public ServerProcess(Server serverReference) {
		this.server = serverReference;
		processServerRunCommand();
	}

	/**
	 * Is used to indicate if the ServerProcess was initialized properly
	 * @return True if it was initialized properly, false if something went wrong
	 */
	public boolean isInitialized() {
		return this.server != null && this.serverRunCommands != null && proc != null;
	}

	/**
	 * This method can be invoked to signal the thread to kill itself and the
	 * process. It will also handle removing it from the linked list.
	 */
	public void terminateServer() {
		server.bot.removeServerFromLinkedList(this.server);
		proc.destroy();
	}

	/**
	 * Parse the server object and run the server based on the configuration
	 */
	private void processServerRunCommand() {

		// Create an arraylist with all our strings
		serverRunCommands = new ArrayList<String>();

		// This must always be first (we may also want a custom binary, so do that here as well)
		serverRunCommands.add(server.executableType);

		// Check if we have a temporary port
		// This will try to host the server on the same port as before
		if (server.temp_port != 0)
			addParameter("-port", String.valueOf(server.temp_port));
		else
			addParameter("-port", Integer.toString(server.bot.getMinPort()));

		// Load the global configuration file
		addParameter("+exec", server.bot.cfg_data.bot_cfg_directory_path + "global.cfg");

		// Create a custom wadpage for us
		String key = MySQL.createWadPage(Functions.implode(this.server.wads, ","));

		// Add the custom page to sv_website to avoid large wad list lookups
		addParameter("+sv_website", "http://www.best-ever.org/wadpage?key=" + key);

		if (server.iwad != null)
			addParameter("-iwad", server.bot.cfg_data.bot_iwad_directory_path + server.iwad);

		if (server.enable_skulltag_data) {
			// Add the skulltag_* data files first since they need to be accessed by other wads
			server.wads.add(0, "skulltag_actors_1-1-1.pk3");
			server.wads.add(1, "skulltag_data_126.pk3");
		}

		// Add the extra wads and clean duplicates
		server.wads.addAll(server.bot.cfg_data.bot_extra_wads);
		server.wads = Functions.removeDuplicateWads(server.wads);

		// Finally, add the wads
		if (server.wads.size() > 0) {
			for (String wad : server.wads) {
				if (Server.isIwad(wad))
					addParameter("-file", server.bot.cfg_data.bot_iwad_directory_path + wad);
				else
					addParameter("-file", server.bot.cfg_data.bot_wad_directory_path + wad);
			}
		}

		if (server.skill != -1)
			addParameter("+skill", String.valueOf(server.skill));

		if (server.gamemode != null)
			addParameter("+" + server.gamemode, " 1");

		if (server.dmflags > 0)
			addParameter("+dmflags", Integer.toString(server.dmflags));

		if (server.dmflags2 > 0)
			addParameter("+dmflags2", Integer.toString(server.dmflags2));

		if (server.dmflags3 > 0)
			addParameter("+dmflags3", Integer.toString(server.dmflags3));

		if (server.compatflags > 0)
			addParameter("+compatflags", Integer.toString(server.compatflags));

		if (server.compatflags2 > 0)
			addParameter("+compatflags2", Integer.toString(server.compatflags2));

		if (server.instagib)
			addParameter("+instagib", "1");

		if (server.buckshot)
			addParameter("+buckshot", "1");

		if (server.servername != null)
			addParameter("+sv_hostname", server.bot.cfg_data.bot_hostname_base + " " + server.servername);

		if (server.config != null)
			addParameter("+exec", server.bot.cfg_data.bot_cfg_directory_path + server.config);

		// Add rcon/file based stuff
		addParameter("+sv_rconpassword", server.server_id);
		addParameter("+sv_banfile", server.bot.cfg_data.bot_banlistdir + server.server_id + ".txt");
		addParameter("+sv_adminlistfile", server.bot.cfg_data.bot_adminlistdir + server.server_id + ".txt");
		addParameter("+sv_banexemptionfile", server.bot.cfg_data.bot_whitelistdir + server.server_id + ".txt");

		// Add the RCON
		server.rcon_password = server.server_id;
	}

	/**
	 * Adds a parameter to the server run command araylist
	 * @param parameter String - parameter
	 * @param argument String - argument
	 */
	public void addParameter(String parameter, String argument) {
		serverRunCommands.add(parameter);
		serverRunCommands.add(argument);
	}

	/**
	 * This method should be executed when the data is set up to initialize the
	 * server. It will be bound to this thread. Upon server termination this
	 * thread will also end. <br>
	 * Note that this method takes care of adding it to the linked list, so you
	 * don't have to.
	 */
	@Override
	public void run() {
		String portNumber = ""; // This will hold the port number
		File logFile, banlist, whitelist, adminlist;
		String strLine, dateNow;
		server.time_started = System.currentTimeMillis();
		last_activity = System.currentTimeMillis(); // Last activity should be when we start
		BufferedReader br = null;
		BufferedWriter bw = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		try {
			// Ensure we have the files created
			banlist = new File(server.bot.cfg_data.bot_banlistdir + server.server_id + ".txt");
			System.out.println(server.bot.cfg_data.bot_banlistdir + server.server_id + ".txt");
			if (!banlist.exists())
				banlist.createNewFile();
			whitelist = new File(server.bot.cfg_data.bot_whitelistdir + server.server_id + ".txt");
			if (!whitelist.exists())
				whitelist.createNewFile();
			adminlist = new File(server.bot.cfg_data.bot_adminlistdir + server.server_id + ".txt");
			if (!adminlist.exists())
				adminlist.createNewFile();

			// Set up the server
			ProcessBuilder pb = new ProcessBuilder(serverRunCommands.toArray(new String[serverRunCommands.size()]));
			// Redirect stderr to stdout
			pb.redirectErrorStream(true);
			// Set our working directory
			pb.directory(new File(server.bot.cfg_data.doom_executable_path));
			proc = pb.start();
			br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			// Set up the input (with autoflush)
			server.in = new PrintWriter(proc.getOutputStream(), true);

			// Set up file/IO
			logFile = new File(server.bot.cfg_data.bot_logfiledir + server.server_id + ".txt");
			bw = new BufferedWriter(new FileWriter(server.bot.cfg_data.bot_logfiledir + server.server_id + ".txt"));

			// Create the logfile
			if (!logFile.exists())
				logFile.createNewFile();

			// Check if global RCON variable is set, or if the user has access to the RCON portion
			// If either criteria is met, the user will be messaged the RCON password
			// NOTE: As of now, BE users can still check the RCON password by accessing the control panel on the website.
			// We'll fix this later by changing the RCON from the unique_id to a random MD5 hash
			if (server.bot.cfg_data.bot_public_rcon || AccountType.isAccountTypeOf(server.user_level, AccountType.ADMIN, AccountType.MODERATOR, AccountType.RCON))
				server.bot.sendMessage(server.sender, "Your unique server ID is: " + server.server_id + ". This is your RCON password, which can be used using 'send_password "+server.server_id+"' via the in-game console. You can view your logfile at http://static.best-ever.org/logs/" + server.server_id + ".txt");

			// Process server while it outputs text
			while ((strLine = br.readLine()) != null) {
				String[] keywords = strLine.split(" ");
				// Make sure to get the port [Server using alternate port 10666.]
				if (strLine.startsWith("Server using alternate port ")) {
					System.out.println(strLine);
					portNumber = strLine.replace("Server using alternate port ", "").replace(".", "").trim();
					if (Functions.isNumeric(portNumber)) {
						server.port = Integer.parseInt(portNumber);
					} else
						server.bot.sendMessage(server.irc_channel, "Warning: port parsing error when setting up server [1]; contact an administrator.");

				// If the port is used [NETWORK_Construct: Couldn't bind to 10666. Binding to 10667 instead...]
				} else if (strLine.startsWith("NETWORK_Construct: Couldn't bind to ")) {
					System.out.println(strLine);
					portNumber = strLine.replace("NETWORK_Construct: Couldn't bind to " + portNumber + ". Binding to ", "").replace(" instead...", "").trim();
					if (Functions.isNumeric(portNumber)) {
						server.port = Integer.parseInt(portNumber);
					} else
						server.bot.sendMessage(server.irc_channel, "Warning: port parsing error when setting up server [2]; contact an administrator.");
				}

				// If we see this, the server started
				if (strLine.equalsIgnoreCase("UDP Initialized.")) {
					System.out.println(strLine);
					server.bot.servers.add(server);
					server.bot.sendMessage(server.irc_channel, "Server started successfully on port " + server.port + "!");
					server.bot.sendMessage(server.sender, "To kill your server, in the channel " + server.bot.cfg_data.irc_channel + ", type .killmine to kill all of your servers, or .kill " + server.port + " to kill just this one.");
				}

				// Check for banned players
				if (keywords[0].equals("CONNECTION")) {
					String ip = keywords[keywords.length-1].split(":")[0];
					String pIP;
					if ((pIP = MySQL.checkBanned(ip)) != null)
						server.in.println("addban " + pIP + " perm \"You have been banned from Best Ever. If you feel that this is an error, please visit irc.zandronum.com #bestever.\"");
				}

				// Check for RCON password changes
				if (keywords.length > 3) {
					if (keywords[0].equals("->") && keywords[1].equalsIgnoreCase("sv_rconpassword"))
						server.rcon_password = keywords[2];
					else if (keywords[0].equalsIgnoreCase("\"sv_rconpassword\""))
						server.rcon_password = keywords[2].replace("\"","");
				}

				// If we have a player joining or leaving, mark this server as active
				if (strLine.endsWith("has connected.") || strLine.endsWith("disconnected."))
					last_activity = System.currentTimeMillis();

				dateNow = formatter.format(Calendar.getInstance().getTime());
				bw.write(dateNow + " " + strLine + "\n");
				bw.flush();
			}

			// Handle cleanup
			dateNow = formatter.format(Calendar.getInstance().getTime());
			long end = System.currentTimeMillis();
			long uptime = end - server.time_started;
			bw.write(dateNow + " Server stopped! Uptime was " + Functions.calculateTime(uptime));
			server.in.close();

			// Notify the main channel if enabled
			if (!server.hide_stop_message) {
				if (server.port != 0)
					server.bot.sendMessage(server.irc_channel, "Server stopped on port " + server.port + "! Server ran for " + Functions.calculateTime(uptime));
				else
					server.bot.sendMessage(server.irc_channel, "Server was not started. This is most likely due to a wad error.");
			}

			// Remove from the Linked List
			server.bot.removeServerFromLinkedList(this.server);

			// Auto-restart the server if enabled, and only if successfully started
			if (server.auto_restart && server.port != 0) {
				server.temp_port = server.port;
				server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Server crashed! Attempting to restart server...");
				server.bot.processHost(server.user_level, server.bot.cfg_data.irc_channel, server.sender, server.irc_hostname, server.host_command, true, server.port);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (br != null)
					br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
