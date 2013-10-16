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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class ConfigData {
	
	/**
	 * Contains the path to the config file
	 */
	public String filepath;
	
	/**
	 * The name of the irc bot
	 */
	public String irc_name;
	
	/**
	 * The username of the irc bot
	 */
	public String irc_user;
	
	/**
	 * The version of the irc bot
	 */
	public String irc_version;
	
	/**
	 * The network it will connect to
	 */
	public String irc_network;
	
	/**
	 * The channel it will connect to
	 */
	public String irc_channel;
	
	/**
	 * The port to which to connect with IRC
	 */
	public int irc_port;
	
	/**
	 * The password to connect to IRC with
	 */
	public String irc_pass;

	/**
	 * The IRC hostname mask (default: .users.zandronum.com)
	 */
	public String irc_mask;

	/**
	 * The mysql host
	 */
	public String mysql_host;
	
	/**
	 * The username for mysql
	 */
	public String mysql_user;
	
	/**
	 * The password for the database
	 */
	public String mysql_pass;
	
	/**
	 * The database for mysql
	 */
	public String mysql_db;
	
	/**
	 * The port for the database, int was used to include all port ranges since shorts cut off signed at 32767
	 */
	public int mysql_port;

	/**
	 * The lowest port number, int was used to include all port ranges since shorts cut off signed at 32767
	 */
	public int bot_min_port;
	
	/**
	 * The highest port number, int was used to include all port ranges since shorts cut off signed at 32767
	 */
	public int bot_max_port;
	
	/**
	 * The logfile path
	 */
	public String bot_logfile;
	
	/**
	 * Contains a path to the root directory for the bot (ex: /home/zandronum/)
	 */
	public String bot_directory_path;
	
	/**
	 * Contains a path to the wad directory
	 */
	public String bot_wad_directory_path;

	/**
	 * Contains a path to the iwad directory
	 */
	public String bot_iwad_directory_path;
	
	/**
	 * Contains a path to the cfg directory
	 */
	public String bot_cfg_directory_path;

	/**
	 * Contains a path to the white list directory
	 */
	public String bot_whitelistdir;

	/**
	 * Contains a path to the ban list directory
	 */
	public String bot_banlistdir;

	/**
	 * Contains a path to the admin list directory
	 */
	public String bot_adminlistdir;

	/**
	 * Log file directory
	 */
	public String bot_logfiledir;
	
	/**
	 * Contains the file name of the executable, in linux this would be "./zandronum-server" for example, or in windows "zandronum.exe"
	 */
	public String bot_executable;
	
	/**
	 * Contains the file name of the kpatch executable (ex: zandronum-server_kpatch)
	 */
	public String bot_executable_kpatch;
	
	/**
	 * Contains the hg build of the latest repository (may not be stable)
	 */
	public String bot_executable_developerrepository;

	/**
	 * Contains whether or not server RCON is accessible to everyone
	 */
	public boolean bot_public_rcon;
	
	/**
	 * The account file path
	 */
	public String bot_accountfile;
	
	/**
	 * If the bot will be verbose or not
	 */
	public boolean bot_verbose;

	/**
	 * Holds the help string
	 */
	public String bot_help;

	/**
	 * If set, this message will broadcast to all servers in bot_notice_interval time
	 */
	public String bot_notice;

	/**
	 * Interval time for broadcast of bot_notice (seconds)
	 */
	public int bot_notice_interval;

	/**
	 * Contains the base of the hostname that you wish to append to servers at
	 * the beginning of their sv_hostname
	 */
	public String bot_hostname_base;
	
	/**
	 * This constructor once initialized will parse the config file based on the path
	 * @param filepath A string with the path to a file
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws InvalidFileFormatException 
	 */
	public ConfigData(String filepath) throws IOException, NumberFormatException {
		this.filepath = filepath;
		parseConfigFile();
	}
	
	/**
	 * This parses the config file and fills up the fields for this object
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws InvalidFileFormatException 
	 */
	private void parseConfigFile() throws IOException, NumberFormatException {
		
		// Initialize the ini object
		Ini ini = new Ini();
		
		// Set up the file
		File configFile = new File(this.filepath);
		
		// Load from file
		ini.load(new FileReader(configFile));
		
		// Load the IRC section
		Ini.Section irc = ini.get("irc");
		this.irc_channel = irc.get("channel");
		this.irc_name = irc.get("name");
		this.irc_user = irc.get("user");
		this.irc_version = irc.get("version");
		this.irc_network = irc.get("network");
		this.irc_pass = irc.get("pass");
		this.irc_port = Integer.parseInt(irc.get("port"));
		this.irc_mask = irc.get("hostmask");
		
		// Load the MYSQL section
		Ini.Section mysql = ini.get("mysql");
		this.mysql_user = mysql.get("user");
		this.mysql_db = mysql.get("db");
		this.mysql_host = mysql.get("host");
		this.mysql_pass = mysql.get("pass");
		this.mysql_port = Integer.parseInt(mysql.get("port"));
		
		// Load the bot section
		Ini.Section bot = ini.get("bot");
		this.bot_accountfile = bot.get("accountfile");
		this.bot_logfile = bot.get("logfile");
		this.bot_max_port = Integer.parseInt(bot.get("max_port"));
		this.bot_min_port = Integer.parseInt(bot.get("min_port"));
		this.bot_verbose = Boolean.parseBoolean(bot.get("verbose"));
		this.bot_directory_path = bot.get("directory");
		this.bot_wad_directory_path = bot.get("waddir");
		this.bot_iwad_directory_path = bot.get("iwaddir");
		this.bot_cfg_directory_path = bot.get("cfgdir");
		this.bot_whitelistdir = bot.get("whitelistdir");
		this.bot_banlistdir = bot.get("banlistdir");
		this.bot_adminlistdir = bot.get("adminlistdir");
		this.bot_logfiledir = bot.get("logfiledir");
		this.bot_executable = bot.get("executable");
		this.bot_executable_kpatch = bot.get("executable_kpatch");
		this.bot_executable_developerrepository = bot.get("executable_developerrepository");
		this.bot_public_rcon = Boolean.parseBoolean(bot.get("public_rcon"));
		this.bot_hostname_base = bot.get("hostname_base");
		this.bot_help = bot.get("help");
		if (bot.get("notice") != null)
			this.bot_notice = bot.get("notice");
		if (bot.get("notice_interval") != null)
			this.bot_notice_interval = Integer.parseInt(bot.get("notice_interval"));
	}
}