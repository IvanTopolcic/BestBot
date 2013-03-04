package org.bestever.bebot;

import static org.bestever.bebot.Logger.logMessage;

import java.io.IOException;
import java.util.LinkedList;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;

/**
 * This is where the bot methods are run and handling of channel/PM input are processed
 */
public class Bot extends PircBot {
	
	/**
	 * Contains the version constant for the bot
	 */
	public static final String BESTBOT_VERSION = "0.1.1";
	
	/**
	 * Contains the config data
	 */
	public ConfigData cfg_data;
	
	/**
	 * Contained a array list of all the servers
	 */
	public LinkedList<Server> servers;
	
	/**
	 * Set the bot up with the constructor
	 */
	public Bot(ConfigData cfg_data) {
		// Point our config data to what we created back in RunMe.java
		this.cfg_data = cfg_data;
		
		// Set up the logger
		Logger.setLogFile(this.cfg_data.bot_logfile);
		
		// Set up MySQL information
		MySQL.setMySQLInformation(this.cfg_data.mysql_host, this.cfg_data.mysql_user, this.cfg_data.mysql_pass, this.cfg_data.mysql_port, this.cfg_data.mysql_db);
		
		// Set up the bot and join the channel
		logMessage("Initializing BestBot v" + getBestBotVersion());
		setVerbose(this.cfg_data.bot_verbose);
		setName(this.cfg_data.irc_name);
		setLogin(this.cfg_data.irc_user);
		setVersion(this.cfg_data.irc_version);
		try {
			connect(this.cfg_data.irc_network, this.cfg_data.irc_port, this.cfg_data.irc_pass);
		} catch (IOException | IrcException e) {
			logMessage("Exception occured while connecting to the network, terminating bot");
			disconnect();
			System.exit(0);
			e.printStackTrace();
		}
		joinChannel(this.cfg_data.irc_channel);
		
		// Set up the server arrays
		this.servers = new LinkedList<Server>();
		
		// Clear mySQL table since we will fill it up with any serialized server information
		if (!MySQL.clearActiveServerList())
			logMessage("ERROR: Could not clear active server list.");
	}
	
	/**
	 * Returns the version of the bestbot running
	 * @return A string containing the version (probably a double in string format)
	 */
	public String getBestBotVersion() {
		return BESTBOT_VERSION;
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
					{
						// We should not use the host result for anything else other than debugging, so I'm having it go out of scope asap
						String hostResult = Server.handleHostCommand(servers, channel, sender, login, hostname, message); // Have this function handle everything
						// Null means nothing went wrong, so don't send anything
						if (hostResult != null)
							sendMessage(cfg_data.irc_channel, hostResult);
					}
					break;
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
		@SuppressWarnings("unused")
		Bot bot = new Bot(cfg_data);
	}
}