package org.bestever.bebot;

import static org.bestever.bebot.Logger.logMessage;
import static org.bestever.bebot.AccountType.*;

import java.io.IOException;
import java.lang.reflect.Field;
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
		
		// Set up the uptime checker
		// Thread r = new Thread(new Uptime(this));
		// r.start();
		
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
	public Server getAllServers() {
		if (servers == null || servers.isEmpty())
			return null;
		ListIterator<Server> it = servers.listIterator();
		Server desiredServer = null;
		while (it.hasNext())
			return desiredServer;
		return null;
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
		Server targetServer = getServer(port);
		if (targetServer != null)
			targetServer.serverprocess.terminateServer();
		else
			sendMessage(cfg_data.irc_channel, "Could not find a server with the port " + port + "!");
	}
	
	/**
	 * Counts the number of players active on a port
	 * @param portString The port to check
	 */
	private void countPlayers(String portString) {
		// Ensure it is a valid port
		if (!Functions.isNumeric(portString)) {
			sendMessage(cfg_data.irc_channel, "Invalid port number (" + portString + "), not terminating server.");
			return;
		}
		
		// Search the port
		int port = Integer.parseInt(portString);
		Server targetServer = getServer(port);
		if (targetServer != null)
			sendMessage(cfg_data.irc_channel, "Number of players: " + targetServer.players);
		else
			sendMessage(cfg_data.irc_channel, "Unable to get server at port " + port + ".");
	}

	/**
	 * Have the bot handle message events
	 */
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		// Perform these only if the message starts with a period (to save processing time on trivial chat)
		if (message.startsWith(".")) {
			// Generate an array of keywords from the message
			String[] keywords = message.split(" ");
			
			// Perform function based on input (note: login is handled by the MySQL function/class)
			// Also mostly in alphabetical order for convenience
			int userLevel = mysql.getLevel(hostname);
			switch (keywords[0].toLowerCase()) {
				case ".commands":
					break;
				case ".file":
					break;
				case ".givememoney":
					sendMessage(cfg_data.irc_channel, Functions.giveMeMoney());
					break;
				case ".help":
					sendMessage(cfg_data.irc_channel, "Please visit http://www.best-ever.org/ for a tutorial on how to set up servers.");
					break;	
				case ".host":
					if (botEnabled) {
						if (isAccountTypeOf(userLevel, ADMIN, MODERATOR, REGISTERED)) {
							Server.handleHostCommand(this, servers, channel, sender, login, hostname, message); // Have this function handle everything
						}
					}
					break;
				case ".kill":
					if (isAccountTypeOf(userLevel, ADMIN, MODERATOR, REGISTERED)) {
						sendMessage(cfg_data.irc_channel, "Attempting to kill: '" + keywords[1] + "'");
						killServer(keywords[1]); // Can pass string, will process it in the method safely if something goes wrong
					}
					break;
				case ".killall":
					break; 
				case ".killmine":
					break; 
				case ".level":
					sendMessage(cfg_data.irc_channel, mysql.getLevel(hostname) + "");
					break;
				case ".load":
					break;
				case ".off":
					if (botEnabled) {
						if (isAccountTypeOf(userLevel, ADMIN)) {
							botEnabled = true;
							sendMessage(cfg_data.irc_channel, "Bot disabled.");
						}
					}
					break;
				case ".on":
					if (!botEnabled) {
						if (isAccountTypeOf(userLevel, ADMIN)) {
							botEnabled = true;
							sendMessage(cfg_data.irc_channel, "Bot enabled.");
						}
					}
					break;
				case ".owner":
					break;
				case ".players":
					countPlayers(keywords[1]);
					break;
				case ".rcon":
					break;
				case ".save":
					break;
				case ".slot":
					break;
				case ".quit":
					if (isAccountTypeOf(userLevel, ADMIN)) {
						this.disconnect();
						System.exit(0);
					}
					break;
				case ".reflect":
					if (isAccountTypeOf(userLevel, ADMIN)) {
						reflect(keywords);
					}
					break;
				default:
					break;
			}
		}
	}
	
	/**
	 * This is a debug function to get fields, it will iterate through the keywords and get methods.
	 * This method is not complete at all yet; also very messy
	 * @param keywords The keywords passed to the reflect function (keyword[0] should be ".reflect")
	 */
	private void reflect(String[] keywords) {
		if (keywords.length < 3) {
			sendMessage(cfg_data.irc_channel, ".reflect usage: .reflect <port> <field/class> [subfield/subclass...]");
			return;
		}
		// Ensure it is a valid port
		if (!Functions.isNumeric(keywords[1])) {
			sendMessage(cfg_data.irc_channel, "Invalid port number (" + keywords[1] + ").");
			return;
		}
		
		// Search the port
		int port = Integer.parseInt(keywords[1]);
		Server server = getServer(port);
		try {
			Field targetField = server.getClass().getDeclaredField(keywords[2]);
			try {
				sendMessage(cfg_data.irc_channel, "Reflected (" + port + ") [ " + keywords[2] + " = Field: " + targetField.get(server).toString() + " ]");
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException nsfe) {
			sendMessage(cfg_data.irc_channel, "No such field to reflect.");
		}
	}

	/**
	 * Have the bot handle private message events
	 */
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		// As of now, you can only perform commands if you are logged in, so we don't need an else here
		if (Functions.checkLoggedIn(hostname)) {
			// Generate an array of keywords from the message (similar to onMessage)
			String[] keywords = message.split(" ");
			
			// Handle private text
			switch (keywords[0].toLowerCase()) {
			case "activate":
				break;
			case "changepw":
				if (keywords.length == 2)
					mysql.changePassword(hostname, keywords[1], sender);
				else
					sendMessage(sender, "Incorrect syntax! Usage is: changepw <new_password>");
				break;
			case "register":
				if (keywords.length == 2)
					mysql.registerAccount(hostname, keywords[1], sender);
				else
					sendMessage(sender, "Incorrect syntax! Usage is: register <password>");
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