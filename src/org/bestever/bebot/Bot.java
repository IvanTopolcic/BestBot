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

import static org.bestever.bebot.Logger.logMessage;
import static org.bestever.bebot.AccountType.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;

/**
 * This is where the bot methods are run and handling of channel/PM input are processed
 */
public class Bot extends PircBot {
	
	/**
	 * Contains the MySQL information
	 */
	public MySQL mysql;
	
	/**
	 * Contains the config data
	 */
	public ConfigData cfg_data;
	
	/**
	 * Contained a array list of all the servers
	 */
	public LinkedList<Server> servers;
	
	/**
	 * Contains the runtime args
	 */
	public String[] args;
	
	/**
	 * The lowest port (the base port) that the bot uses
	 */
	private int min_port;
	
	/**
	 * The highest included port number that the bot uses
	 */
	private int max_port;
	
	/**
	 * A toggle variable for allowing hosting
	 */
	public boolean botEnabled = true;
	
	/**
	 * Set the bot up with the constructor
	 */
	public Bot(ConfigData cfgfile) {
		// Point our config data to what we created back in RunMe.java
		cfg_data = cfgfile;
		
		// Set up the logger
		Logger.setLogFile(cfg_data.bot_logfile);
		
		// Set up the bot and join the channel
		logMessage("Initializing BestBot v" + cfg_data.irc_version);
		setVerbose(cfg_data.bot_verbose);
		setName(cfg_data.irc_name);
		setLogin(cfg_data.irc_user);
		setVersion(cfg_data.irc_version);
		try {
			connect(cfg_data.irc_network, cfg_data.irc_port, cfg_data.irc_pass);
		} catch (IOException | IrcException e) {
			logMessage("Exception occured while connecting to the network, terminating bot");
			disconnect();
			System.exit(0);
			e.printStackTrace();
		}
		joinChannel(cfg_data.irc_channel);
		
		// Set initial ports
		this.min_port = cfg_data.bot_min_port;
		this.max_port = cfg_data.bot_max_port;
		
		// Set up the server arrays
		this.servers = new LinkedList<Server>();
		
		// Set up MySQL
		mysql = new MySQL(this, cfg_data.mysql_host, cfg_data.mysql_user, cfg_data.mysql_pass, cfg_data.mysql_port, cfg_data.mysql_db);
		
		// Clear mySQL table since we will fill it up with any serialized server information
		mysql.clearActiveServerList();
	}
	
	/**
	 * Gets the minimum port to be used by the bot
	 * @return An integer containing the minimum port used
	 */
	public int getMinPort() {
		return min_port;
	}

	/**
	 * Returns the max port used by the bot
	 * @return An integer containing the max port used
	 */
	public int getMaxPort() {
		return max_port;
	}

	/**
	 * This method attempts to set the max port to the specified value, if there
	 * is an odd number entered (<= min_port) then it will not set it
	 * @param max_port The desired change to a maximum port
	 * @return True if the port was changed, false if there was an error
	 */
	public boolean setMaxPort(int max_port) {
		if (max_port <= min_port)
			return false;
		else if (max_port == this.max_port)
			return true; // Don't change anything
		this.max_port = max_port;
		return true;
	}
	
	/**
	 * This function goes through the linkedlist of servers and removes servers
	 * @param server
	 */
	public void removeServerFromLinkedList(Server server) {
		if (servers == null || servers.isEmpty())
			return;
		ListIterator<Server> it = servers.listIterator();
		while (it.hasNext()) {
			// Check if they refer to the exact same object
			if (it.next() == server) {
				it.remove();
				return;
			}
		}
	}
	
	/**
	 * Returns a Server from the linked list based on the port number provided
	 * @param port The port to check
	 * @return The server object reference if it exists, null if there's no such object
	 */
	public Server getServer(int port) {
		if (servers == null | servers.isEmpty()) 
			return null;
		ListIterator<Server> it = servers.listIterator();
		Server desiredServer = null;
		while (it.hasNext()) {
			desiredServer = it.next();
			if (desiredServer.port == port)
				return desiredServer;
		}
		return null;
	}
	
	/**
	 * Returns all of the servers in the linked list
	 * @return The server object
	 */
	// Need to check if it works
	public Server[] getAllServers() {
		if (servers == null || servers.isEmpty())
			return null;
		Server[] serverList = new Server[servers.size()];
		serverList = servers.toArray(serverList);
		return serverList;
	}
	
	/**
	 * This searches through the linkedlist to kill the server on that port,
	 * the method does not actually kill it, but signals a boolean to terminate
	 * which the thread that is running it will handle the termination itself and
	 * removal from the linkedlist.
	 * @param portString The port desired to kill
	 */
	private void killServer(String portString) {
		// Ensure it is a valid port
		if (!Functions.isNumeric(portString)) {
			sendMessage(cfg_data.irc_channel, "Invalid port number (" + portString + "), not terminating server.");
			return;
		}
		
		// Since our port is numeric, parse it
		int port = Integer.parseInt(portString);
		
		// Handle users sending in a small value (thus saving time 
		if (port < min_port) {
			sendMessage(cfg_data.irc_channel, "Invalid port number (ports start at " + min_port + "), not terminating server.");
			return;
		}
		
		// See if the port is in our linked list, if so signify for it to die
		Server targetServer = getServer(port);
		if (targetServer != null)
			targetServer.killServer();
		else
			sendMessage(cfg_data.irc_channel, "Could not find a server with the port " + port + "!");
	}

	/**
	 * Have the bot handle message events
	 */
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		// Perform these only if the message starts with a period (to save processing time on trivial chat)
		if (message.startsWith(".")) {
			// Generate an array of keywords from the message
			String[] keywords = message.split(" ");
			
			// Perform function based on input (note: login is handled by the MySQL function/class); also mostly in alphabetical order for convenience
			int userLevel = mysql.getLevel(hostname);
			switch (keywords[0].toLowerCase()) {
				case ".commands":
					sendMessage(cfg_data.irc_channel, "Allowed commands: " + processCommands(userLevel));
					break;
				case ".file":
					processFile(userLevel, keywords, channel);
					break;
				case ".get":
					processGet(userLevel, keywords);
					break;
				case ".givememoney":
					sendMessage(cfg_data.irc_channel, Functions.giveMeMoney());
					break;
				case ".help":
					sendMessage(cfg_data.irc_channel, "Please visit http://www.best-ever.org/ for a tutorial on how to set up servers.");
					break;	
				case ".host":
					processHost(userLevel, channel, sender, login, hostname, message);
					break;
				case ".kill":
					processKill(userLevel, keywords, sender);
					break;
				case ".killall":
					processKillAll(userLevel, keywords);
					break;
				case ".killmine":
					processKillMine(userLevel, keywords, hostname);
					break;
				case ".killinactive":
					processKillInactive(userLevel, keywords);
					break;
				case ".load":
					processLoad(userLevel, keywords);
					break;
				case ".numservers":
					processNumServers(userLevel, keywords);
					break;
				case ".off":
					processOff(userLevel);
					break;
				case ".on":
					processOn(userLevel);
					break;
				case ".owner":
					processOwner(userLevel, keywords);
					break;
				case ".players":
					processPlayers(userLevel, keywords);
					break;
				case ".quit":
					processQuit(userLevel);
					break;
				case ".rcon":
					if (isAccountTypeOf(userLevel, ADMIN, MODERATOR, REGISTERED))
						sendMessage(cfg_data.irc_channel, "Please PM the bot for your rcon.");
					break;
				case ".save":
					processSave(userLevel, keywords);
					break;
				case ".slot":
					processSlot(userLevel, keywords);
					break;
				case ".userlevel":
					processUserLevel(userLevel, hostname);
					break;
				default:
					break;
			}
		}
	}

	/**
	 * This displays commands available for the user
	 * @param userLevel The level based on AccountType enumeration
	 */
	private String processCommands(int userLevel) {
		switch (userLevel) {
		case GUEST:
			return "[Not logged in, guests have limited access] .commands, .file, .givememoney, .help";
		case REGISTERED:
			return ".commands, .file, .get, .givememoney, .help, .host, .kill, .killmine, .load, .owner, .players, .rcon, .save, .slot";
		case MODERATOR:
			return ".commands, .file, .get, .givememoney, .help, .host, .kill, .killmine, .killinactive, .load, .owner, .players, .rcon, .save, .slot, .userlevel";
		case ADMIN:
			return ".commands, .file, .get, .givememoney, .help, .host, .kill, .killall, .killmine, .killinactive, .load, .on, .off, .owner, .players, .quit, .rcon, .save, .slot, .userlevel";
		}
		return "Undocumented type. Contact an administrator.";
	}
	
	/**
	 * This checks to see if the file exists in the wad directory (it is lower-cased)
	 * @param userLevel The level of the user
	 * @param keywords The keywords sent (should be a length of two)
	 * @param channel The channel to respond to
	 */
	private void processFile(int userLevel, String[] keywords, String channel) {
		if (keywords.length == 2) {
			File file = new File(cfg_data.bot_wad_directory_path + keywords[1].toLowerCase());
			if (file.exists())
				sendMessage(channel, "File " + keywords[1].toLowerCase() + " exists on the server.");
			else
				sendMessage(channel, "File does not exist.");
		} else
			sendMessage(channel, "Incorrect syntax, use: .file <filename.wad/pk3>");
	}
	
	/**
	 * Gets a field requested by the user
	 * @param userLevel The user's bitmask level
	 * @param keywords The field the user wants
	 */
	private void processGet(int userLevel, String[] keywords) {
		if (isAccountTypeOf(userLevel, ADMIN, MODERATOR, REGISTERED)) {
			if (keywords.length != 3) {
				sendMessage(cfg_data.irc_channel, "Proper syntax: .get <port> <property> -- see http://www.best-ever.org for what properties you can get");
				return;
			}
			if (!Functions.isNumeric(keywords[1])) {
				sendMessage(cfg_data.irc_channel, "Port is not a valid number");
				return;
			}
			Server tempServer = getServer(Integer.parseInt(keywords[1]));
			if (tempServer == null) {
				return;
			}
			sendMessage(cfg_data.irc_channel, tempServer.getField(keywords[1]));
		}
	}
	
	/**
	 * Passes the host command off to a static method to create the server
	 * @param userLevel The user's bitmask level
	 * @param channel IRC data associated with the sender
	 * @param sender IRC data associated with the sender
	 * @param login IRC data associated with the sender
	 * @param hostname IRC data associated with the sender
	 * @param message The entire message to be processed
	 */
	private void processHost(int userLevel, String channel, String sender, String login, String hostname, String message) {
		if (botEnabled)
			if (isAccountTypeOf(userLevel, ADMIN, MODERATOR, REGISTERED))
				Server.handleHostCommand(this, servers, channel, sender, login, hostname, message); // Have this function handle everything
			else
				sendMessage(cfg_data.irc_channel, "You must register and be logged in to IRC to use the bot to host!");
		else
			sendMessage(cfg_data.irc_channel, "The bot is currently disabled from hosting for the time being. Sorry for any inconvenience!");
	}
	
	/**
	 * Attempts to kill a server based on the port
	 * @param userLevel The user's bitmask level
	 * @param keywords The keywords to be processed
	 * @param sender The person sending the request
	 */
	private void processKill(int userLevel, String[] keywords, String sender) {
		// Ensure proper syntax
		if (keywords.length != 2) {
			sendMessage(cfg_data.irc_channel, "Proper syntax: .kill <port>");
			return;
		}
		
		// Safety net
		if (servers == null) {
			sendMessage(cfg_data.irc_channel, "Major error: Linkedlist is null, contact an administrator.");
			return;
		}
		
		// If server list is empty
		if (servers.isEmpty()) { 
			sendMessage(cfg_data.irc_channel, "There are currently no servers running!");
			return;
		}
		
		// Registered can only kill their own servers
		if (isAccountTypeOf(userLevel, REGISTERED)) {
			if (Functions.isNumeric(keywords[1])) {
				Server server = getServer(Integer.parseInt(keywords[1]));
				if (server != null)
					if (server.sender.toLowerCase().equals(sender))
						if (server.serverprocess != null)
							server.serverprocess.terminateServer();
						else
							sendMessage(cfg_data.irc_channel, "Error: Server process is null, contact an administrator.");
					else
						sendMessage(cfg_data.irc_channel, "Error: You do not own this server!");
				else
					sendMessage(cfg_data.irc_channel, "Error getting server, please contact an administrator.");
			} else 
				sendMessage(cfg_data.irc_channel, "Improper port number.");
		// Admins/mods can kill anything
		} else if (isAccountTypeOf(userLevel, ADMIN, MODERATOR)) {
			sendMessage(cfg_data.irc_channel, "Attempting to kill server on port " + keywords[1] + "...");
			killServer(keywords[1]); // Can pass string, will process it in the method safely if something goes wrong
		}
	}
	
	/**
	 * When requested it will kill every server in the linked list
	 * @param userLevel The user level of the person requesting
	 * @param keywords The keywords requested
	 */
	private void processKillAll(int userLevel, String[] keywords) {
		if (isAccountTypeOf(userLevel, ADMIN)) {
			if (servers != null && servers.size() > 0) {
				sendMessage(cfg_data.irc_channel, "ATTENTION: Terminating all servers...");
				ListIterator<Server> li = servers.listIterator();
				while (li.hasNext())
					li.next().killServer();
				sendMessage(cfg_data.irc_channel, "Servers termination request complete");
			} else
				sendMessage(cfg_data.irc_channel, "No servers to kill (" + (servers == null ? "LinkedList is null" : "No servers") + ").");
		}
	}
	
	/**
	 * This will look through the list and kill all the servers that the hostname owns
	 * @param userLevel The level of the user
	 * @param keywords The keywords requested
	 * @param hostname The hostname of the person invoking this command
	 */
	private void processKillMine(int userLevel, String[] keywords, String hostname) {
		if (isAccountTypeOf(userLevel, ADMIN, MODERATOR, REGISTERED)) {
			if (servers != null && servers.size() > 0) {
				ListIterator<Server> li = servers.listIterator();
				Server s;
				while (li.hasNext()) {
					s = li.next();
					if (s.irc_hostname.equals(hostname))
						s.killServer();
				}
			} else {
				sendMessage(cfg_data.irc_channel, "No servers to kill (" + (servers == null ? "LinkedList is null" : "No servers") + ").");
			}
		}
	}
	
	/**
	 * This will kill inactive servers based on the days specified in the second parameter
	 * @param userLevel The user's bitmask level
	 * @param keywords The field the user wants
	 */
	private void processKillInactive(int userLevel, String[] keywords) {
		if (isAccountTypeOf(userLevel, ADMIN, MODERATOR)) {
			if (keywords.length < 2) {
				sendMessage(cfg_data.irc_channel, "Proper syntax: .killinactive <days since> (ex: use .killinactive 3 to kill servers that haven't seen anyone for 3 days)");
				return;
			}
			if (Functions.isNumeric(keywords[1])) {
				int numOfDays = Integer.parseInt(keywords[1]);
				if (numOfDays > 0) {
					if (servers == null || servers.isEmpty()) {
						sendMessage(cfg_data.irc_channel, "No servers to kill.");
						return;
					}
					sendMessage(cfg_data.irc_channel, "Killing servers that are " + numOfDays + " days old or older...");
					ListIterator<Server> it = servers.listIterator();
					Server s = null; 
					while (it.hasNext()) {
						s = it.next();
						if (System.currentTimeMillis() - s.time_started > (Server.DAY_MILLISECONDS * numOfDays))
							s.serverprocess.terminateServer();
					}
				} else {
					sendMessage(cfg_data.irc_channel, "Using zero or less for .killinactive is not allowed.");
					return;
				}
			} else {
				sendMessage(cfg_data.irc_channel, "Unexpected parameter for method.");
				return;
			}
		}
	}
	
	// UNIMPLEMENTED YET
	private void processLoad(int userLevel, String[] keywords) {
		if (isAccountTypeOf(userLevel, ADMIN, MODERATOR, REGISTERED)) {
        }
	}
	
	/**
	 * Sends a string to the channel with how many total servers are running
	 * @param userLevel The level of the user invoking
	 * @param keywords The keywords sent
	 */
	private void processNumServers(int userLevel, String[] keywords) {
		if (keywords.length == 1)
			sendMessage(cfg_data.irc_channel, "There are " + servers.size() + " servers running on Best Ever right now.");
		else
			sendMessage(cfg_data.irc_channel, "Improper syntax, use: .numservers");
	}
	
	/**
	 * Admins can turn off hosting with this
	 * @param userLevel The user's bitmask level
	 */
	private void processOff(int userLevel) {
		if (botEnabled) {
			if (isAccountTypeOf(userLevel, ADMIN)) {
				botEnabled = true;
				sendMessage(cfg_data.irc_channel, "Bot disabled.");
			}
		}
	}
	
	/**
	 * Admins can re-enable hosting with this
	 * @param userLevel The user's bitmask level
	 */
	private void processOn(int userLevel) {
		if (!botEnabled) {
			if (isAccountTypeOf(userLevel, ADMIN)) {
				botEnabled = true;
				sendMessage(cfg_data.irc_channel, "Bot enabled.");
			}
		}
	}
	
	/**
	 * This checks for who owns the server on the specified port
	 * @param userLevel The level of the user requesting the data
	 * @param keywords The keywords to pass
	 */
	private void processOwner(int userLevel, String[] keywords) {
		if (isAccountTypeOf(userLevel, ADMIN, MODERATOR, REGISTERED)) {
			if (keywords.length == 2) {
				if (Functions.isNumeric(keywords[1])) {
					Server s = getServer(Integer.parseInt(keywords[1]));
					if (s != null)
						sendMessage(cfg_data.irc_channel, "The owner of port " + keywords[1] + " is: " + s.irc_hostname + "[" + s.irc_login + "].");
					else
						sendMessage(cfg_data.irc_channel, "No server indexed on port " + keywords[1] + ".");
				} else
					sendMessage(cfg_data.irc_channel, "Invalid port number.");
			} else
				sendMessage(cfg_data.irc_channel, "Improper syntax, use: .owner <port>");
		}
	}
	
	/**
	 * Gets the number of players in a server
	 * @param userLevel The user's bitmask level
	 * @param keywords The keywords to be processed
	 */
	private void processPlayers(int userLevel, String[] keywords) {
		// Ensure it is a valid port
		if (!Functions.isNumeric(keywords[1])) {
			sendMessage(cfg_data.irc_channel, "Invalid port number (" + keywords[1] + ").");
			return;
		}
		
		// Search the port
		int port = Integer.parseInt(keywords[1]);
		Server targetServer = getServer(port);
		if (targetServer != null)
			sendMessage(cfg_data.irc_channel, "Number of players: " + targetServer.players);
		else
			sendMessage(cfg_data.irc_channel, "Unable to get server at port " + port + ".");
	}
	
	
	private void processRcon(int userLevel, String[] keywords, String sender) {
		// Admins should see everything
		if (isAccountTypeOf(userLevel, MODERATOR, ADMIN)) {
			
		// Registered should only see their own server
		} else if (isAccountTypeOf(userLevel, REGISTERED)) {
			
		}
	}
	
	// UNIMPLEMENTED YET
	private void processSave(int userLevel, String[] keywords) {
		if (isAccountTypeOf(userLevel, ADMIN, MODERATOR, REGISTERED)) {
        }
	}

	// UNIMPLEMENTED YET
	private void processSlot(int userLevel, String[] keywords) {
		if (isAccountTypeOf(userLevel, ADMIN, MODERATOR, REGISTERED)) {
        }
	}
	

	/**
	 * Invoking this command terminates the bot completely
	 * @param userLevel The user's bitmask level
	 */
	private void processQuit(int userLevel) {
		if (isAccountTypeOf(userLevel, ADMIN)) {
			this.disconnect();
			System.exit(0);
		}
	}
	
	/**
	 * Returns the level of the user <br>
	 * Note: Does not support looking up an alias username at the moment
	 * @param userLevel The level of the invoker requesting the data
	 * @param hostname The name of the user (this is hostname, not logged in IRC name)
	 */
	private void processUserLevel(int userLevel, String hostname) {
		if (isAccountTypeOf(userLevel, ADMIN, MODERATOR))
			sendMessage(cfg_data.irc_channel, "User level of '" + hostname + "': " +Integer.toString(mysql.getLevel(hostname)));
	}

	/**
	 * Have the bot handle private message events
	 * @param sender The IRC data of the sender
	 * @param login The IRC data of the sender
	 * @param hostname The IRC data of the sender
	 * @param message The message transmitted
	 */
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		// As of now, you can only perform commands if you are logged in, so we don't need an else here
		if (Functions.checkLoggedIn(hostname)) {
			// Generate an array of keywords from the message (similar to onMessage)
			String[] keywords = message.split(" ");
			int userLevel = mysql.getLevel(hostname);
			switch (keywords[0].toLowerCase()) {
				case ".rcon":
					if (keywords.length == 2)
						processRcon(userLevel, keywords, sender);
					else
						sendMessage(sender, "Incorrect syntax! Usage is: /msg " + cfg_data.irc_name + " rcon <port>");
					break;
				case "changepass":
				case "changepassword":
				case "changepw":
					if (keywords.length == 2)
						mysql.changePassword(hostname, keywords[1], sender);
					else
						sendMessage(sender, "Incorrect syntax! Usage is: /msg " + cfg_data.irc_name + " changepw <new_password>");
					break;
				case "register":
					if (keywords.length == 2)
						mysql.registerAccount(hostname, keywords[1], sender);
					else
						sendMessage(sender, "Incorrect syntax! Usage is: /msg " + cfg_data.irc_name + " register <password>");
					break;
				default:
					break;
			}
		} else {
			sendMessage(cfg_data.irc_channel, "Your account is not logged in properly to the IRC network. Please log in and re-query.");
		}
	}
	
	/**
	 * Contains the main methods that are run on start up for the bot
	 * The arguments should contain the path to the Bot.cfg file only
	 */
	public static void main(String[] args) {
		// We need only one argument to the config 
		if (args.length != 1) {
			System.out.println("Incorrect arguments, please have only one arg to your ini path");
			return;
		}
		
		// Attempt to load the config
		ConfigData cfg_data;
		try {
			cfg_data = new ConfigData(args[0]);
		} catch (NumberFormatException e) {
			System.out.println("Warning: ini file has a string where a number should be!");
			e.printStackTrace();
			return;
		} catch (IOException e) {
			System.out.println("Warning: ini file IOException!");
			e.printStackTrace();
			return;
		}
		
		// Start the bot
		Bot bot = new Bot(cfg_data);
		bot.args = args;
	}
}
