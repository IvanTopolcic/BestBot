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

import java.io.File;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bestever.bebot.Logger.*;
import static org.bestever.bebot.MySQL.SERVER_ONLINE;

public class Server {
	/**
	 * If true, servers will not say "server stopped on port..."
	 */
	public boolean hide_stop_message = false;

	/**
	 * Holds the input stream
	 */
	public PrintWriter in;

	/**
	 * Contains whether or not the server should auto-restart when terminated
	 */
	public boolean auto_restart = false;

	/**
	 * Contains the thread of the server process
	 */
	public ServerProcess serverprocess;
	
	/**
	 * Contains the reference to the bot
	 */
	public Bot bot;

	/**
	 * Contains the port it is run on
	 */
	public int port;
	
	/**
	 * The time the server was started
	 */
	public long time_started;
	
	/**
	 * This is the generated password at the very start for server logs and the password
	 */
	public String server_id;
	
	/**
	 * Username of the person who sent the command to start it
	 */
	public String sender;
	
	/**
	 * The channel it was hosted from
	 */
	public String irc_channel;
	
	/**
	 * This is the host's hostname on irc
	 */
	public String irc_hostname;
	
	/**
	 * This is the login name used
	 */
	public String irc_login;
	
	/**
	 * Contains the entire ".host" command
	 */
	public String host_command;

	/**
	 * Contains the level of the user
	 */
	public int user_level;
	
	/**
	 * Contains the hostname used, this will NOT contain " :: [BE] New York "
	 */
	public String servername;

	/**
	 * This is the iwad used
	 */
	public String iwad;

	/**
	 * Contains the gamemode
	 */
	public String gamemode;

	/**
	 * The name of the config file (like rofl.cfg), will contain ".cfg" on the end of the string
	 */
	public String config;
	
	/**
	 * Contains a list of all the wads used by the server separated by a space
	 */
	public String[] wads;

	/**
	 * Contains a list of all the wads separated by a space which will be searched for maps
	 */
	public String[] mapwads;

	/**
	 *  Holds the skill of the game
	 */
	public int skill;

	/**
	 * If this is true, that means skulltag data will be enabled
	 */
	public boolean enable_skulltag_data;
	
	/**
	 * If this is true, instagib will be enabled on the server
	 */
	public boolean instagib;
	
	/**
	 * If this is true, buckshot will be enabled on the server
	 */
	public boolean buckshot;

	/**
	 * Contains flags for the server
	 */
	public int dmflags;
	
	/**
	 * Contains flags for the server
	 */
	public int dmflags2;
	
	/**
	 * Contains flags for the server
	 */
	public int dmflags3;
	
	/**
	 * Contains flags for the server
	 */
	public int compatflags;
	
	/**
	 * Contains flags for the server
	 */
	public int compatflags2;
	
	/**
	 * Contains the play_time in percentage
	 */
	public long play_time = 0;
	
	/**
	 * Contains the RCON Password
	 */
	public String rcon_password;
	
	/**
	 * If there's an error with processing of numbers, return this
	 */
	public static final int FLAGS_ERROR = 0xFFFFFFFF;
	
	/**
	 * This is the time of a day in milliseconds
	 */
	public static final long DAY_MILLISECONDS = 1000 * 60 * 60 * 24;
	
	/**
	 * Default constructor for building a server
	 */
	public Server() {
		// Purposely empty
	}
	
	/**
	 * This will take ".host ...", parse it and pass it off safely to anything else
	 * that needs the information to create/run the servers and the mysql database.
	 * In addition, all servers will be passed onto a server queue that will use a
	 * thread which processes them one by one from the queue to prevent two servers
	 * attempting to use the same port at the same time
	 * @param botReference The reference to the running bot
	 * @param servers The linkedlist of servers for us to add on a server if successful
	 * @param channel The channel it was sent from
	 * @param hostname The hostname of the sender
	 * @param message The message sent
	 * @return Null if all went well, otherwise an error message to print to the bot
	 */
	public static void handleHostCommand(Bot botReference, LinkedList<Server> servers, String channel, String sender, String hostname, String message, int userLevel, boolean autoRestart) {
		// Initialize server without linking it to the arraylist
		Server server = new Server();
		
		// Reference server to bot
		server.bot = botReference;

		// Check if autoRestart was enabled
		if (autoRestart)
			server.auto_restart = true;
		
		// Input basic values
		server.irc_channel = channel;
		server.irc_hostname = hostname;
		server.host_command = message;
		server.user_level = userLevel;
		server.sender = sender;

		// Regex that will match key=value, as well as quotes key="value"
		Pattern regex = Pattern.compile("(\\w+)=\"*((?<=\")[^\"]+(?=\")|([^\\s]+))\"*");
		Matcher m = regex.matcher(message);

		// While we have a key=value
		while (m.find()) {
			switch (m.group(1)) {
				case "buckshot":
					server.buckshot = handleTrue(m.group(2));
					break;
				case "compatflags":
					server.compatflags = handleGameFlags(m.group(2));
					if (server.compatflags == FLAGS_ERROR) {
						server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Problem with parsing compatflags");
						return;
					}
					break;
				case "compatflags2":
					server.compatflags2 = handleGameFlags(m.group(2));
					if (server.compatflags == FLAGS_ERROR) {
						server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Problem with parsing compatflags2");
						return;
					}
					break;
				case "config":
					if (!server.checkConfig(server.bot.cfg_data.bot_cfg_directory_path + Functions.cleanInputFile(m.group(2).toLowerCase()))) {
						server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Config file '" + m.group(2) + "' does not exist.");
						return;
					}
					server.config = Functions.cleanInputFile(m.group(2).toLowerCase());
					break;
				case "data":
				case "stdata":
					server.enable_skulltag_data = handleTrue(m.group(2));
					break;
				case "dmflags":
					server.dmflags = handleGameFlags(m.group(2));
					if (server.dmflags == FLAGS_ERROR) {
						server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Problem with parsing dmflags");
						return;
					}
					break;
				case "dmflags2":
					server.dmflags2 = handleGameFlags(m.group(2));
					if (server.dmflags2 == FLAGS_ERROR) {
						server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Problem with parsing dmflags2");
						return;
					}
					break;
				case "dmflags3":
					server.dmflags3 = handleGameFlags(m.group(2));
					if (server.dmflags3 == FLAGS_ERROR) {
						server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Problem with parsing dmflags3");
						return;
					}
					break;
				case "gamemode":
					server.gamemode = getGamemode(m.group(2));
					break;
				case "hostname":
					server.servername = m.group(2);
					break;
				case "instagib":
					server.instagib = handleTrue(m.group(2));
					break;
				case "iwad":
					server.iwad = getIwad(Functions.cleanInputFile(m.group(2)));
					break;
				case "mapwad":
					server.mapwads = addWads(m.group(2));
					break;
				case "skill":
					server.skill = handleSkill(m.group(2));
					if (server.skill == -1) {
						server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Skill must be between 0-4");
						return;
					}
					break;
				case "wad":
					server.wads = addWads(m.group(2));
					break;
			}
		}

		// Check if the wads exist
		if (server.wads != null) {
			for (String wad : server.wads) {
				if (!Functions.fileExists(server.bot.cfg_data.bot_wad_directory_path + wad)) {
					server.bot.sendMessage(server.bot.cfg_data.irc_channel, "File '" + wad + "' does not exist!");
					return;
				}
			}
		}

		// Check if mapwad exists
		if (server.mapwads != null) {
			for (String wad : server.mapwads) {
				if (!Functions.fileExists(server.bot.cfg_data.bot_wad_directory_path + wad)) {
					server.bot.sendMessage(server.bot.cfg_data.irc_channel, "File '" + wad + "' does not exist!");
					return;
				}
			}
		}

		// Now that we've indexed the string, check to see if we have what we need to start a server
		if (server.iwad == null) {
			server.bot.sendMessage(server.bot.cfg_data.irc_channel, "You are missing an iwad! You can add it by appending: iwad=your_iwad");
			return;
		}
		if (server.gamemode == null) {
			server.bot.sendMessage(server.bot.cfg_data.irc_channel, "You are missing the gamemode! You can add it by appending: gamemode=your_gamemode");
			return;
		}
		if (server.servername == null) {
			server.bot.sendMessage(server.bot.cfg_data.irc_channel, "You are missing the hostname! You can add it by appending: hostname=\"Your Server Name\"");
			return;
		}

		// Check if the global server limit has been reached
		if (Functions.getFirstAvailablePort(server.bot.getMinPort(), server.bot.getMaxPort()) == 0) {
			server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Global server limit has been reached.");
		}

		// Generate the unique ID
		try {
			server.server_id = Functions.generateHash();
		} catch (NoSuchAlgorithmException e) {
			logMessage(LOGLEVEL_CRITICAL, "Error generating MD5 hash!");
			server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Error generating MD5 hash. Please contact an administrator.");
			return;
		}

		// Assign and start a new thread
		server.serverprocess = new ServerProcess(server);
		server.serverprocess.start();
		botReference.mysql.logServer(server.servername, server.server_id, Functions.getUserName(server.irc_hostname));
	}
	
	/**
	 * Servers stored in the database should be loaded upon invoking this
	 * function on bot startup, the bot should call the MySQL's 
	 * {@link org.bestever.bebot.MySQL#pullServerData pullServerData() method}.
	 * This will automatically (assuming there isn't a MySQL error) begin to get
	 * the servers up and running and fill the objects with the appropriate 
	 * information.
	 * @param bot The calling bot reference.
	 */
	public void loadServers(Bot bot, ResultSet rs) {
		// If something goes wrong...
		if (rs == null) {
			logMessage(LOGLEVEL_CRITICAL, "Unable to load servers from MySQL!");
			return;
		}
		
		// Go through each server and initialize them accordingly
		int database_id = -1;
		try {
			Server server;
			while (rs.next()) {
				// The server should be marked as online, if it's not then skip it
				if (rs.getInt("online") == SERVER_ONLINE) {
					database_id = rs.getInt("id");
					server = new Server(); // Reference a new object each time we run through the servers
					server.bot = bot;
					server.buckshot = (rs.getInt("buckshot") == 1 ? true : false);
					server.compatflags = rs.getInt("compatflags");
					server.compatflags2 = rs.getInt("compatflags2");
					server.config = rs.getString("config");
					server.dmflags = rs.getInt("dmflags");
					server.dmflags2 = rs.getInt("dmflags2");
					server.dmflags3 = rs.getInt("dmflags3");
					server.enable_skulltag_data = (rs.getInt("enable_skulltag_data") == 1 ? true : false);
					server.gamemode = rs.getString("gamemode");
					server.host_command = rs.getString("host_command");
					server.instagib = (rs.getInt("instagib") == 1 ? true : false);
					server.irc_channel = rs.getString("irc_channel");
					server.irc_hostname = rs.getString("irc_hostname");
					server.irc_login = rs.getString("irc_login");
					server.iwad = rs.getString("iwad");
					server.mapwads = rs.getString("mapwads").replace(" ","").split(","); // Check this!
					// server.play_time = 0; // NOT IN THE DATABASE
					server.rcon_password = rs.getString("rcon_password");
					server.sender = rs.getString("username"); // ???
					server.server_id = rs.getString("unique_id"); // ???
					server.servername = rs.getString("servername");
					server.time_started = rs.getLong("time_started");
					server.user_level = 0; // ??? Get from Mysql
					server.wads = rs.getString("wads").replace(" ","").split(","); // Check this!
					
					// Handle the server (pass it to the appropriate places before referencing a new object) (server.port and server.serverprocess)
					logMessage(LOGLEVEL_NORMAL, "Successfully processed server id " + database_id + "'s data.");
					server.serverprocess = new ServerProcess(server);
					server.serverprocess.start();
				}
			}
		} catch (SQLException e) {
			logMessage(LOGLEVEL_CRITICAL, "MySQL exception loading servers at " + database_id + "!");
			e.printStackTrace();
		}
	}

	/**
	 * Checks to see if the configuration file exists
	 * @param config String - config name
	 * @return true/false
	 */
	private boolean checkConfig(String config) {
		File f = new File(config);
		if (f.exists())
			return true;
		else
			return false;
	}

	/**
	 * Returns the skill of the game
	 * @param skill String - skill level
	 * @return int - skill level
	 */
	private static int handleSkill(String skill) {
		if (!Functions.isNumeric(skill) || Integer.parseInt(skill) > 4 || Integer.parseInt(skill) < 0) {
			return -1;
		}
		else
			return Integer.parseInt(skill);
	}

	/**
	 * Returns an array of wads from a String
	 * @param wad comma-seperated list of wads
	 * @return array of wads
	 */
	private static String[] addWads(String wad) {
		String[] wads = wad.split(",");
		for (int i = 0; i < wads.length; i++)
			wads[i] = wads[i].trim().toLowerCase();
		return wads;
	}

	/**
	 * Checks for the iwad based on the input
	 * @param string The keyword with the iwad (ex: iwad=doom2.wad)
	 * @return A string of the wad (lowercase), or null if there's no supported iwad name
	 */
	private static String getIwad(String string) {
		// Check if in array, and if so return that value
		switch (string.toLowerCase()) {
			case "doom2":
			case "doom2.wad":
				return "doom2.wad";
			case "doom":
			case "doom.wad":
				return "doom.wad";
			case "tnt":
			case "tnt.wad":
				return "tnt.wad";
			case "plutonia":
			case "plutonia.wad":
				return "plutonia.wad";
			case "heretic":
			case "heretic.wad":
				return "heretic.wad";
			case "hexen":
			case "hexen.wad":
				return "hexen.wad";
			case "strife1":
			case "strife1.wad":
				return "strife1.wad";
			case "sharewaredoom":
			case "doom1":
			case "doom1.wad":
				return "doom1.wad";
			case "hacx":
			case "hacx.wad":
				return "hacx.wad";
			case "chex3":
			case "chex3.wad":
				return "chex3.wad";
			case "megaman":
			case "megagame":
			case "megagame.wad":
				return "megagame.wad";
			case "freedm":
			case "freedm.wad":
				return "freedm.wad";
			case "nerve":
			case "nerve.wad":
				return "nerve.wad";
		}
		// If there's no match...
		return null;
	}

	/**
	 * Takes input to parse the gamemode 
	 * @param string The keyword to check with the = sign (ex: gamemode=...)
	 * @return A string of the gamemode, null if there was no such gamemode
	 */
	private static String getGamemode(String string) {
		// Find out if the string we're given matches a game mode
		switch (string.toLowerCase())
		{
			case "deathmatch":
			case "dm":
			case "ffa":
				return "deathmatch";
			case "ctf":
				return "ctf";
			case "tdm":
			case "teamdm":
			case "tdeathmatch":
			case "teamdeathmatch":
				return "teamplay";
			case "terminator":
				return "terminator";
			case "possession":
				return "possession";
			case "teampossession":
				return "teampossession";
			case "lms":
			case "lastmanstanding":
				return "lastmanstanding";
			case "tlms":
			case "teamlms":
			case "teamlastmanstanding":
				return "teamlms";
			case "skulltag":
				return "skulltag";
			case "duel":
				return "duel";
			case "teamgame":
				return "teamgame";
			case "domination":
				return "domination";
			case "coop":
			case "co-op":
			case "cooperative":
				return "cooperative";
			case "survival":
				return "survival";
			case "invasion":
				return "invasion";
			case "oneflagctf":
				return "oneflagctf"; // NEEDS SUPPORT (please check)
		}
		
		// If the gametype is unknown, return null
		return null;
	}

	/**
	 * Method that contains aliases for on/off properties
	 * @param string The keyword to check
	 * @return True if to use it, false if not
	 */
	private static boolean handleTrue(String string) {
		switch (string.toLowerCase()) {
			case "on":
			case "true":
			case "yes":
			case "enable":
				return true;
		}
		// Otherwise if something is wrong, just assume we need it
		return false;
	}

	/**
	 * This handles dmflags/compatflags, returns 0xFFFFFFFF if there's an error (FLAGS_ERROR)
	 * @param keyword The keyword to check
	 * @return A number of what it is
	 */
	private static int handleGameFlags(String keyword) {
		// If the right side is numeric and passes some logic checks, return that as the flag
		int flag = 0;
		if (Functions.isNumeric(keyword))
			flag = Integer.parseInt(keyword);
		if (flag >= 0)
			return flag;
		
		// If something went wrong, return an error
		return FLAGS_ERROR;
	}
	
	/**
	 * Will return generic things from a server that a user may want to request, this method
	 * does not return anything that contains sensitive information (which can be done with reflection)
	 * @param fieldToGet A String indicating what field to get
	 * @return A String containing the data
	 */
	public String getField(String fieldToGet) {
		switch (fieldToGet.toLowerCase()) {
			case "buckshot":
				return "buckshot: " + Boolean.toString(this.buckshot);
			case "compatflags":
				return "compatflags: " + Integer.toString(this.compatflags);
			case "compatflags2":
				return "compatflags2: " + Integer.toString(this.compatflags2);
			case "config":
			case "cfg":
			case "configuration":
				return "config: " + nullToNone(this.config);
			case "data":
			case "enable_skulltag_data":
			case "stdata":
			case "skulltag_data":
			case "skulltagdata":
				return "data: " + Boolean.toString(this.enable_skulltag_data);
			case "dmflags":
				return "dmflags: " + Integer.toString(this.dmflags);
			case "dmflags2":
				return "dmflags2 " + Integer.toString(this.dmflags2);
			case "dmflags3":
				return "dmflags3 " + Integer.toString(this.dmflags3);
			case "gamemode":
			case "gametype":
				return "gamemode " + this.gamemode;
			case "host":
			case "hostcommand":
			case "host_command":
				return "hostcommand: " + this.host_command;
			case "instagib":
				return "instagib: " + Boolean.toString(this.instagib);
			case "iwad":
				return "iwad: " + this.iwad;
			case "mapwad":
			case "mapwads":
				return "mapwads: " + checkWads(this.mapwads);
			case "name":
			case "server_name":
			case "hostname":
			case "servername":
				return "hostname: " + this.servername;
			case "skill":
				return "skill: " + this.skill;
			case "wad":
			case "wads":
				return "wads: " + checkWads(this.wads);
			default:
				break;
		}
		return "Error: Not a supported keyword";
	}

	/**
	 * Checks for null values and returns and more user friendly message
	 * @param input String[] input
	 * @return String[] result
	 */
	public String checkWads(String[] input) {
		if (input == null)
			return "None";
		else if (Functions.implode(this.wads, ", ") == null)
			return input[0];
		else
			return Functions.implode(input, ", ");
	}

	/**
	 * Checks for null values and returns and more user friendly message
	 * @param input String input
	 * @return String result
	 */
	public String nullToNone(String input) {
		if (input == null)
			return "None";
		else return input;
	}
	
	/**
	 * Checks to see if the core stuff is needed to start a server
	 * @return True if it's a valid server, false otherwise
	 */
	public boolean validServer() {
		if (this.time_started == 0 || this.play_time == 0)
			return false;
		if (this.sender == null) 
			return false;
		if (this.irc_channel == null) 
			return false;
		if (this.irc_hostname == null)
			return false;
		if (this.irc_login == null)
			return false;
		if (this.iwad == null)
			return false;
		if (this.gamemode == null)
			return false;
		if (this.rcon_password == null)
			return false;
		if (this.server_id == null)
			return false;
		if (this.servername == null)
			return false;
		return true;
	}
	
	/**
	 * This will kill the server
	 */
	public void killServer() {
		if (this.serverprocess != null && this.serverprocess.isInitialized())
			this.serverprocess.terminateServer();
	}
}