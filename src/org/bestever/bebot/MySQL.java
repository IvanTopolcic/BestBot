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

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.bestever.bebot.Logger.*;

/**
 * MySQL Class for handling all of the database inserts/fetching
 */
public class MySQL {
	
	/**
	 * Holds the Connection
	 */
	private Connection con;
	
	/**
	 * Holds the Bot
	 */
	private Bot bot;
	
	/**
	 * Holds the MySQL hostname
	 */
	private String mysql_host;
	
	/**
	 * Holds the MySQL username
	 */
	private String mysql_user;
	
	/**
	 * Holds the MySQL password
	 */
	private String mysql_pass;
	
	/**
	 * Holds the MySQL port
	 */
	private int mysql_port;
	
	/**
	 * Holds the MySQL database
	 */
	private String mysql_db;
	
	/**
	 * A constant in the database to indicate a server is considered online
	 */
	public static final int SERVER_ONLINE = 1;
	
	/**
	 * Constructor for the MySQL Object
	 * @param bot instance of the bot
	 * @param host MySQL hostname
	 * @param user MySQL username
	 * @param pass MySQL Password
	 * @param port MySQL Port
	 * @param db MySQL Database
	 */
	public MySQL(Bot bot, String host, String user, String pass, int port, String db) {
		this.bot = bot;
		this.mysql_host = host;
		this.mysql_user = user;
		this.mysql_pass = pass;
		this.mysql_port = port;
		this.mysql_db = db;
        try {
        	Class.forName("com.mysql.jdbc.Driver");
			this.con = DriverManager.getConnection("jdbc:mysql://" + mysql_host + ":"+mysql_port+"/", mysql_user, mysql_pass);
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("Could not connect to MySQL Database!");
			logMessage(LOGLEVEL_CRITICAL, "Error connecting to MySQL database!");
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Gets the maximum number of servers the user is allowed to host
	 * @param hostname String - the user's hostname
	 * @return server_limit Int - maximum server limit of the user
	 */
	public int getMaxSlots(String hostname) {
		if (Functions.checkLoggedIn(hostname)) {
			String query = "SELECT `server_limit` FROM " + mysql_db + ".`login` WHERE `username` = ?";
			try (PreparedStatement pst = con.prepareStatement(query)) {
				pst.setString(1, Functions.getUserName(hostname));
				ResultSet r = pst.executeQuery();
				if (r.next())
					return r.getInt("server_limit");
				else
					return 0;
			} catch (SQLException e) {
				System.out.println("ERROR: SQL_ERROR in 'getMaxSlots()'");
				logMessage(LOGLEVEL_IMPORTANT, "ERROR: SQL_ERROR in 'getMaxSlots()'");
				e.printStackTrace();
			}
		}
		return AccountType.GUEST; // Return 0, which is a guest and means it was not found; also returns this if not logged in
	}
	
	/**
	 * Queries the database and returns the level of the user
	 * @param hostname of the user
	 * @return level for success, 0 for fail, -1 for non-existant username
	 */
	public int getLevel(String hostname) {
		if (Functions.checkLoggedIn(hostname)) {
			String query = "SELECT `level` FROM " + mysql_db + ".`login` WHERE `username` = ?";
			try (PreparedStatement pst = con.prepareStatement(query)) {
				pst.setString(1, Functions.getUserName(hostname));
				ResultSet r = pst.executeQuery();
				if (r.next())
					return r.getInt("level");
				else
					return 0;
			} catch (SQLException e) {
				System.out.println("ERROR: SQL_ERROR in 'getLevel()'");
				logMessage(LOGLEVEL_IMPORTANT, "ERROR: SQL_ERROR in 'getLevel()'");
				e.printStackTrace();
			}
		}
		return AccountType.GUEST; // Return 0, which is a guest and means it was not found; also returns this if not logged in
	}
	
	/**
	 * Inserts an account into the database
	 * (assuming the user is logged in to IRC)
	 * @param hostname hostname of the user
	 * @param password password of the user
	 */
	public void registerAccount(String hostname, String password, String sender) {	
		logMessage(LOGLEVEL_NORMAL, "Handling account registration from " + sender + ".");
		// Query to check if the username already exists
		String checkQuery = "SELECT `username` FROM " + mysql_db + ".`login` WHERE `username` = ?";
		
		// Query to add entry to database
		String executeQuery = "INSERT INTO " + mysql_db + ".`login` ( `username`, `password`, `level`, `activated`, `server_limit` ) VALUES ( ?, ?, 0, 1, 1 )";
		try
		(
			PreparedStatement cs = con.prepareStatement(checkQuery);
			PreparedStatement xs = con.prepareStatement(executeQuery)
		){
			// Query and check if see if the username exists
			cs.setString(1, Functions.getUserName(hostname));
			ResultSet r = cs.executeQuery();
			
			// The username already exists!
			if (r.next())
				bot.sendMessage(sender, "Account already exists!");
			else {
				// Prepare, bind & execute
				xs.setString(1, r.getString("username"));
				// Hash the PW with BCrypt
				xs.setString(2, BCrypt.hashpw(password, BCrypt.gensalt(14)));
				if (xs.executeUpdate() == 1)
					this.bot.sendMessage(sender, "Account created! Your username is " + r.getString("username") + " and your password is " + password);
				else
					this.bot.sendMessage(sender, "There was an error registering your account.");
				}
			} catch (SQLException e) {
			System.out.println("ERROR: SQL_ERROR in 'registerAccount()'");
			logMessage(LOGLEVEL_CRITICAL, "ERROR: SQL_ERROR in 'registerAccount()'");
			e.printStackTrace();
			bot.sendMessage(sender, "There was an error registering your account.");
		}
	}
	
	/**
	 * Changes the password of a logged in user
	 * (assuming the user is logged into IRC)
	 * @param hostname the user's hostname
	 * @param password the user's password
	 */
	public void changePassword(String hostname, String password, String sender) {
		logMessage(LOGLEVEL_NORMAL, "Password change request from " + sender + ".");
		// Query to check if the username already exists
		String checkQuery = "SELECT `username` FROM " + mysql_db + ".`login` WHERE `username` = ?";
		
		// Query to update password
		String executeQuery = "UPDATE " + mysql_db + ".`login` SET `password` = ? WHERE `username` = ?";
		try
		(
			PreparedStatement cs = con.prepareStatement(checkQuery);
			PreparedStatement xs = con.prepareStatement(executeQuery)
		){
			// Query and check if see if the username exists
			cs.setString(1, Functions.getUserName(hostname));
			ResultSet r = cs.executeQuery();
			
			// The username doesn't exist!
			if (!r.next())
				bot.sendMessage(sender, "Username does not exist.");
			
			else {
				// Prepare, bind & execute
				xs.setString(1, BCrypt.hashpw(password, BCrypt.gensalt(14)));
				xs.setString(2, r.getString("username"));
				if (xs.executeUpdate() == 1)
					bot.sendMessage(sender, "Successfully changed your password!");
				else
					bot.sendMessage(sender, "There was an error changing your password (executeUpdate error). Try again or contact an administrator with this message.");
			}
		} catch (SQLException e) {
			System.out.println("ERROR: SQL_ERROR in 'changePassword()'");
			logMessage(LOGLEVEL_IMPORTANT, "ERROR: SQL_ERROR in 'changePassword()'");
			e.printStackTrace();
			bot.sendMessage(sender, "There was an error changing your password account (thrown SQLException). Try again or contact an administrator with this message.");
		}
	}

	/**
	 * Saves a server host command to a row
	 * @param hostname String - the user's hostname (for verification)
	 * @param words String Array - array of words
	 */
	public void saveSlot(String hostname, String[] words) {
		if (words.length > 2) {
			String hostmessage = Functions.implode(Arrays.copyOfRange(words, 2, words.length), " ");
			if ((words.length > 2) && (Functions.isNumeric(words[1]))) {
				int slot = Integer.parseInt(words[1]);
				if (slot > 0 && slot < 11) {
					try {
							String query = "SELECT `slot` FROM " + mysql_db + ".`save` WHERE `slot` = ? && `username` = ?";
							PreparedStatement pst = con.prepareStatement(query);
							pst.setInt(1, slot);
							pst.setString(2, Functions.getUserName(hostname));
							ResultSet rs = pst.executeQuery();
							boolean empty = true;
							while (rs.next())
								empty = false;
							if (empty)
								query = "INSERT INTO " + mysql_db + ".`save` (`serverstring`, `slot`, `username`) VALUES (?, ?, ?)";
							else
								query = "UPDATE " + mysql_db + ".`save` SET `serverstring` = ? WHERE `slot` = ? && `username` = ?";
							pst = con.prepareStatement(query);
							pst.setString(1, hostmessage);
							pst.setInt(2, slot);
							pst.setString(3, Functions.getUserName(hostname));
							pst.executeUpdate();
							rs.close();
							bot.sendMessage(bot.cfg_data.irc_channel, "Successfully updated save list.");
						}
					catch (SQLException e) {
						bot.sendMessage(bot.cfg_data.irc_channel, "MySQL error!");
					}
				}
				else {
					bot.sendMessage(bot.cfg_data.irc_channel, "You may only specify slot 1 to 10.");
				}
			}
		}
		else {
			bot.sendMessage(bot.cfg_data.irc_channel, "Incorrect syntax! Correct usage is .save 1-10 <host_message>");
		}
	}

	/**
	 * Loads server saved with the .save command
	 * @param hostname String - their hostname
	 * @param words String[] - their message
	 * @param level Int - their user level
	 * @param channel String - the channel
	 * @param sender String - sender's name
	 * @param login String - sender's login name
	 */
	public void loadSlot(String hostname, String[] words, int level, String channel, String sender, String login) {
		if (words.length == 2) {
			if (Functions.isNumeric(words[1])) {
				int slot = Integer.parseInt(words[1]);
				if (slot > 10 || slot < 1) {
					bot.sendMessage(bot.cfg_data.irc_channel, "Slot must be between 1 and 10.");
					return;
				}
				try {
					String query = "SELECT `serverstring` FROM " + mysql_db + ".`save` WHERE `slot` = ? && `username` = ?";
					PreparedStatement pst = con.prepareStatement(query);
					pst.setInt(1, slot);
					pst.setString(2, Functions.getUserName(hostname));
					ResultSet r = pst.executeQuery();
					if (r.next()) {
						String hostCommand = r.getString("serverstring");
						bot.processHost(level, channel, sender, hostname, hostCommand);
					}
					else {
						 bot.sendMessage(bot.cfg_data.irc_channel, "You do not have anything saved to that slot!");
					}
				}
				catch (SQLException e) {
					bot.sendMessage(bot.cfg_data.irc_channel, "Whoops, something went wrong! If this problem persists, please contact an Administrator!");
					Logger.logMessage(LOGLEVEL_IMPORTANT, "Exception in loadSlot");
					e.printStackTrace();
				}
			}
		}
		else {
			bot.sendMessage(bot.cfg_data.irc_channel, "Incorrect syntax! Correct syntax is .load 1 to 10");
		}
	}

	/**
	 * Logs a server to the database
	 * @param servername String - the name of the server
	 * @param unique_id String - the server's unique ID
	 * @param username String - username of server host
	 */
	public void logServer(String servername, String unique_id, String username) {
		String query = "INSERT INTO `" + mysql_db + "`.`serverlog` (`unique_id`, `servername`, `username`, `date`) VALUES (?, ?, ?, NOW())";
		try (PreparedStatement pst = con.prepareStatement(query)) {
			pst.setString(1, unique_id);
			pst.setString(2, servername);
			pst.setString(3, username);
			pst.executeUpdate();
			pst.close();
		}
		catch (SQLException e) {
			Logger.logMessage(LOGLEVEL_IMPORTANT, "SQLException in logServer()");
			e.printStackTrace();
		}
	}

	/**
	 * Shows a server host string saved with the .save command
	 * @param hostname String - the user's hostname
	 * @param words String[] - array of words of message
	 */
	public void showSlot(String hostname, String[] words) {
		if (words.length == 2) {
			if (Functions.isNumeric(words[1])) {
				int slot = Integer.parseInt(words[1]);
				if (slot > 0 && slot < 11) {
					try {
						String query = "SELECT `serverstring`,`slot` FROM `server`.`save` WHERE `slot` = ? && `username` = ?";
						PreparedStatement pst = con.prepareStatement(query);
						pst.setInt(1, slot);
						pst.setString(2, Functions.getUserName(hostname));
						ResultSet rs = pst.executeQuery();
						if (rs.next())
						{
							bot.sendMessage(bot.cfg_data.irc_channel, "In slot " + rs.getString("slot") + ": " + rs.getString("serverstring"));
						}
						else
						{
							bot.sendMessage(bot.cfg_data.irc_channel, "You do not have anything saved to that slot!");
						}
					}
					catch (SQLException e) {
						bot.sendMessage(bot.cfg_data.irc_channel, "Whoops, something went wrong! If this problem persists, please contact an Administrator!");
						Logger.logMessage(LOGLEVEL_IMPORTANT, "Exception in showSlot");
						e.printStackTrace();
					}
				}
				else {
					bot.sendMessage(bot.cfg_data.irc_channel, "Slot must be between 1 and 10!");
				}
			}
			else {
				bot.sendMessage(bot.cfg_data.irc_channel, "Slot must be a number.");
			}
		}
		else {
			bot.sendMessage(bot.cfg_data.irc_channel, "Incorrect syntax! Correct usage is .load <slot>");
		}
	}
	
	/**
	 * Grabs the data from the mysql database and runs servers by passing their
	 * information off to a method in the bot that will process accordingly.
	 * This should be run at startup and only startup.
	 * @param bot The bot object that will have server data sent to it.
	 */
	public static void pullServerData(Bot bot) {
	}
	
	/**
	 * Writes the server object to the database. This is intended to be for read
	 * only purposes if the bot goes down, and to be possibly used on the site
	 * as a means of displaying information.
	 * @param server The server object by which the data should be written from.
	 */
	public static boolean writeServerData(Server server) {
		//INSERT INTO `server`.`servers` 
		//       (`id`, `unique_id`, `username`, `date`, `time_started`, `sender`, `irc_channel`, `irc_hostname`, `irc_login`, `host_command`, `servername`, `iwad`,   `gamemode`,   `config`,    `wads`, `mapwads`, `enable_skulltag_data`, `instagib`, `buckshot`, `dmflags`, `dmflags2`, `dmflags3`, `compatflags`, `compatflags2`, `online`) 
		//VALUES ('1', 'asdij9dmkma', 'dummy', '2013-01-01',    '0',        'me',    'ircchan',     'irchost',    'irclogin',  '.host crap',      'hello',   'doom2', 'deathmatch', 'rofl.cfg', 'yes.wad', 'no.wad',             '1',           '0',         '0',      '3248',     '284',       '4',        '28248',         '1',         '1');
		return false;
	}
	
	/**
	 * Clears all the servers starting on the specified key up until the max
	 * port. This is to be used after the server data from the database have 
	 * been pulled at startup.
	 * @param primaryKeyToStartAt The ID of the key by which server rows should
	 * have the Online column set to zero.
	 * @return True if it was completed successfully, false if there was any 
	 * kind of SQLException thrown while trying to set the servers.
	 */
	// UNIMPLEMENTED YET
	@SuppressWarnings("unused")
	private boolean clearInactiveServerOnlineColumn(int primaryKeyToStartAt) {
		//try {
		//	
		//} catch (SQLException e) {
		//	e.printStackTrace();
		//	return false;
		//}
		return true;
	}
}
