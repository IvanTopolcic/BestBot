package org.bestever.server;

import java.io.File;
import java.io.FileReader;

import org.bestever.exceptions.ConfigException;
import org.ini4j.Ini;

public class ServerConfigData {

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
	 * The port to listen on for incoming IRC client connections.
	 */
	public int server_listening_port;

	/**
	 * The port base to start hosting from.
	 */
	public int server_hosting_base_port;

	/**
	 * number of servers to host at maximum.
	 */
	public int server_hosting_max_servers;

	/**
	 * The message to be delivered to the server in regular intervals.
	 */
	public String server_notice_message;

	/**
	 * The amount of minutes between each message, unusual numbers default to 60.
	 */
	public int server_notice_interval_minutes;

	/**
	 * A list of files that will always be included in each wad.
	 */
	public String server_extra_wads;

	/**
	 * The base hostname to be applied to all servers.
	 */
	public String server_hostname_base;

	/**
	 * If rcon is allowed to be retrieved by the public.
	 */
	public boolean server_public_rcon;

	/**
	 * MySQL hostname.
	 */
	public String server_mysql_host;

	/**
	 * MySQL username.
	 */
	public String server_mysql_user;

	/**
	 * MySQL password.
	 */
	public String server_mysql_pass;

	/**
	 * MySQL database name.
	 */
	public String server_mysql_db;

	/**
	 * MySQL port number.
	 */
	public int server_mysql_port;

	/**
	 * Location of the adminlist folder to generate files at.
	 */
	public String server_directory_adminlist;

	/**
	 * Location of the banlist folder to generate files at.
	 */
	public String server_directory_banlist;

	/**
	 * Location of the adminlist folder to generate files at.
	 */
	public String server_directory_configs;

	/**
	 * Location of the developer repository binaries.
	 */
	public String server_directory_executable_developer;

	/**
	 * Location of the iwads folder.
	 */
	public String server_directory_iwads;

	/**
	 * Location of the log folder to generate files at.
	 */
	public String server_directory_logfiles;

	/**
	 * Location of the wad directory for uploaded files to be used.
	 */
	public String server_directory_wads;

	/**
	 * Location of the whitelist folder to generate files at.
	 */
	public String server_directory_whitelist;

	/**
	 * Default zandronum executable name.
	 */
	public String server_executable;

	/**
	 * KPatch binary name.
	 */
	public String server_executable_kpatch;

	/**
	 * Developer binary name.
	 */
	public String server_executable_developer;
	
	/**
	 * Creates a filled Server config.
	 * @param iniPath The path to the ini file.
	 * @throws ConfigException If there was any error reading the config flie.
	 */
	public ServerConfigData(String iniPath) throws ConfigException {
		try {
			// Initialize/read the ini object
			Ini ini = new Ini();
			File configFile = new File(iniPath);
			ini.load(new FileReader(configFile));
			
			// Load global data
			Ini.Section global = ini.get("global");
			global_logfile = global.get("logfile");
			global_bestbotpassword = global.get("bestbotpassword");
			
			// Read server specific data
			Ini.Section server = ini.get("server");
			server_listening_port = Integer.parseInt(server.get("listening_port"));
			server_hosting_base_port = Integer.parseInt(server.get("hosting_base_port"));
			server_hosting_max_servers = Integer.parseInt(server.get("hosting_max_servers"));
			server_notice_message = server.get("notice_message");
			server_notice_interval_minutes = Integer.parseInt(server.get("notice_interval_minutes"));
			server_extra_wads = server.get("extra_wads");
			server_hostname_base = server.get("hostname_base");
			server_public_rcon = Boolean.parseBoolean(server.get("public_rcon"));
			server_mysql_host = server.get("mysql_host");
			server_mysql_user = server.get("mysql_user");
			server_mysql_pass = server.get("mysql_pass");
			server_mysql_db = server.get("mysql_db");
			server_mysql_port = Integer.parseInt(server.get("mysql_port"));
			server_directory_adminlist = server.get("directory_adminlist");
			server_directory_banlist = server.get("directory_banlist");
			server_directory_configs = server.get("directory_configs");
			server_directory_executable_developer = server.get("directory_executable_developer");
			server_directory_iwads = server.get("directory_iwads");
			server_directory_logfiles = server.get("directory_logfiles");
			server_directory_wads = server.get("directory_wads");
			server_directory_whitelist = server.get("directory_whitelist");
			server_executable = server.get("executable");
			server_executable_kpatch = server.get("executable_kpatch");
			server_executable_developer = server.get("executable_developer");
		} catch (Exception e) {
			throw new ConfigException(e.getMessage());
		}
	}
}
