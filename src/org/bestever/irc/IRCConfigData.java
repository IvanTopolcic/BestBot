package org.bestever.irc;

import java.io.File;
import java.io.FileReader;

import org.bestever.exceptions.ConfigException;
import org.ini4j.Ini;

public class IRCConfigData {
	
	/**
	 * The logfile to write information to.
	 */
	public String global_logfile;
	
	/**
	 * The password used to authorize an IRC and Server Hoster connection.
	 * This will be hashed up by BCrypt, so it should be the raw password.
	 */
	public String global_bestbotpassword;
	
	/**
	 * The "real life name" of the bot.
	 */
	public String irc_name;
	
	/**
	 * The username of the bot
	 */
	public String irc_username;
	
	/**
	 * The version of the bot.
	 */
	public String irc_version;
	
	/**
	 * The IRC password of the bot.
	 */
	public String irc_pass;
	
	/**
	 * The network the bot will connect to.
	 */
	public String irc_network;
	
	/**
	 * The IRC channel the bot will connect to, read messages from and send
	 * message to.
	 */
	public String irc_channel;
	
	/**
	 * The port to connect to IRC on.
	 */
	public int irc_port;
	
	/**
	 * If the bot should be verbose.
	 */
	public boolean irc_verbose;
	
	/**
	 * What to return when the users type "[token]help" for assistance.
	 */
	public String irc_help;
	
	/**
	 * Whether the bot should allow the public to access rcon or not.
	 */
	public boolean irc_public_rcon;
	
	/**
	 * The token that signals to the bot it's a command. Example: If the token
	 * is a '.' then typing .host would make it read for the host command. If 
	 * the token was '#' then typing #commands would run that command.
	 */
	public String irc_token;
	
	/**
	 * The IP of the server to send data to.
	 */
	public String irc_server_ip;
	
	/**
	 * The port of the server to send data to.
	 */
	public int irc_server_port;
	
	/**
	 * Creates a filled IRC config.
	 * @param iniPath The path to the ini file.
	 * @throws ConfigException If there was any error reading the config flie.
	 */
	public IRCConfigData(String iniPath) throws ConfigException {
		try {
			// Initialize/read the ini object
			Ini ini = new Ini();
			File configFile = new File(iniPath);
			ini.load(new FileReader(configFile));
			
			// Load global data
			Ini.Section global = ini.get("global");
			global_logfile = global.get("logfile");
			global_bestbotpassword = global.get("bestbotpassword");
			
			// Read IRC specific data
			Ini.Section irc = ini.get("irc");
			irc_name = irc.get("name");
			irc_username = irc.get("user");
			irc_version = irc.get("version");
			irc_pass = irc.get("pass");
			irc_network = irc.get("network");
			irc_channel = irc.get("channel");
			irc_port = Integer.parseInt(irc.get("port"));
			irc_verbose = Boolean.parseBoolean(irc.get("verbose"));
			irc_help = irc.get("help");
			irc_public_rcon = Boolean.parseBoolean(irc.get("public_rcon"));
			irc_token = irc.get("token");
			irc_server_ip = irc.get("server_ip");
			irc_server_port = Integer.parseInt(irc.get("server_port"));
		} catch (Exception e) {
			throw new ConfigException(e.getMessage());
		}
	}
}
