package org.bestever.bebot;

import static org.bestever.bebot.Logger.logMessage;

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
	 * Set the bot up with the constructor
	 */
	public Bot(ConfigData cfgfile) {
		// Point our config data to what we created back in RunMe.java
		cfg_data = cfgfile;
		
		// Set up the logger
		Logger.setLogFile(cfg_data.bot_logfile);
		
		// Set up MySQL information
		MySQL.setMySQLInformation(cfg_data.mysql_host, cfg_data.mysql_user, cfg_data.mysql_pass, cfg_data.mysql_port, cfg_data.mysql_db);
		
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
		
		// Clear mySQL table since we will fill it up with any serialized server information
		if (!MySQL.clearActiveServerList())
			logMessage("ERROR: Could not clear active server list.");
	}
	
	/**
	 * Gets the minimum port to be used by the bot
	 * @return An integer containing the minimum port used
	 */
	public int getMinport() {
		return min_port;
	}

	/**
	 * Returns the max port used by the bot
	 * @return An integer containing the max port used
	 */
	public int getMaxport() {
		return max_port;
	}

	/**
	 * This method attempts to set the max port to the specified value, if there
	 * is an odd number entered (<= min_port) then it will not set it
	 * @param max_port The desired change to a maximum port
	 * @return True if the port was changed, false if there was an error
	 */
	public boolean setMaxport(int max_port) {
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
				server.bot.sendMessage(server.channel, "Debug: Successfully removed server from linkedlist ^_^   ~Your truly, BestBot++");
				return;
			}
		}
	}
	
	// Debugging purposes only for now
	public void debug() {
		if (servers.isEmpty()) {
			sendMessage(cfg_data.irc_channel, "No servers in the LinkedList.");
			return;
		}
		ListIterator<Server> it = servers.listIterator();
		Server itServer = null;
		while (it.hasNext()) {
			itServer = it.next();
			if (itServer == null)
				break;
			sendMessage(cfg_data.irc_channel, "Hostname: " + itServer.sv_hostname);
		}
		sendMessage(cfg_data.irc_channel, "Done debug.");
	}
	
	/**
	 * This searches through the linkedlist to kill the server on that port,
	 * the method does not actually kill it, but signals a boolean to terminate
	 * which the thread that is running it will handle the termination itself and
	 * removal from the linkedlist.
	 * @param port The port desired to kill
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
		ListIterator<Server> it = servers.listIterator();
		Server targetServer;
		while (it.hasNext()) {
			targetServer = it.next();
			if (targetServer.port == port) {
				System.out.println(targetServer.hostname + " @ " + targetServer.port + " found! Terminating...");
				targetServer.serverprocess.terminateServer();
				return;
			} else {
				System.out.println(targetServer.hostname + " @ " + targetServer.port + " does not match.");
			}
		}
	}
	
	private void countPlayers(String portString) {
		
		int port = Integer.parseInt(portString);
		
		ListIterator<Server> it = servers.listIterator();
		Server targetServer;
		while (it.hasNext()) {
			targetServer = it.next();
			if (targetServer.port == port) {
				sendMessage(cfg_data.irc_channel, "Number of players: " + targetServer.players);
			}
		}
	}

	/**
	 * Have the bot handle message events
	 */
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		// Perform these only if the message starts with a period (to save processing time on trivial chat)
		if (message.startsWith(".")) {
			// Generate an array of keywords from the message
			String[] keywords = message.split(" ");
			
			// Check if the hoster is logged in
			boolean loggedIn = Functions.checkLoggedIn(hostname);
			
			// Perform only the logged in stuff
			if (loggedIn) {
				switch (keywords[0].toLowerCase()) {
					case ".host":
						Server.handleHostCommand(this, servers, channel, sender, login, hostname, message); // Have this function handle everything
						break;
					case ".quit":
						this.disconnect();
						System.exit(0);
					case ".kill":
						sendMessage(cfg_data.irc_channel, "Attempting to kill: '" + keywords[1] + "'");
						killServer(keywords[1]); // Can pass string, will process it in the method safely if something goes wrong
						killServer(keywords[1].trim());
						break;
					case ".debug":
						debug();
						break;
					case ".players":
						countPlayers(keywords[1]);
					default:
						break;
				}
			// Else if not logged in, perform these
			} else {
				switch (keywords[0].toLowerCase()) {
					case ".help":
						sendMessage(cfg_data.irc_channel, "Please visit http://www.best-ever.org/ for a tutorial on how to set up servers.");
						break;	
					default:
						break;
				}
			}
		}
	}

	/**
	 * Have the bot handle private message events
	 */
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		// Generate an array of keywords from the message (similar to onMessage)
		String[] keywords = message.split(" ");
		
		// Check if the user is logged in
		boolean loggedIn = Functions.checkLoggedIn(hostname);
		
		// As of now, you can only perform commands if you are logged in
		// So we don't need an else here
		if (loggedIn) {
			switch (keywords[0].toLowerCase()) {
			// Registering an account
			case "register":
				if (keywords.length == 2) {
					int value = MySQL.registerAccount(hostname, keywords[1]);
					switch (value) {
					case -1:
						sendMessage(sender, "Account already exists!");
						break;
					case 0:
						sendMessage(sender, "Error adding your account to the database.");
						break;
					case 1:
						sendMessage(sender, "Account registration successful! Your username is " + Functions.getUserName(hostname) + " and your password is " + keywords[1]);
						break;
					}
				}
				else
					sendMessage(sender, "Incorrect syntax! Usage is /msg BestBot register <password>");
				break;
			// Changing user password
			case "changepw":
				int value = MySQL.changePassword(hostname, keywords[1]);
				if (keywords.length == 2) {
					switch (value) {
					case -1:
						sendMessage(sender, "You don't have an account!");
						break;
					case 0:
						sendMessage(sender, "Error updating password.");
						break;
					case 1:
						sendMessage(sender, "Success! Your password was changed to " + keywords[1]);
						break;
					}
				}
				else
					sendMessage(sender, "Incorrect syntax! Usage is /msg BestBot changepw <new_password>");
				break;
			default:
				break;
			}
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