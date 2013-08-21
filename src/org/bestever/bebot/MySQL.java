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
import java.util.LinkedList;
import java.util.List;

import static org.bestever.bebot.Logger.*;

/**
 * MySQL Class for handling all of the database inserts/fetching
 */
public class MySQL {
	
	/**
	 * Holds the Bot
	 */
	private static Bot bot;
	
	/**
	 * Holds the MySQL hostname
	 */
	private static String mysql_host;
	
	/**
	 * Holds the MySQL username
	 */
	private static String mysql_user;
	
	/**
	 * Holds the MySQL password
	 */
	private static String mysql_pass;
	
	/**
	 * Holds the MySQL port
	 */
	private static int mysql_port;
	
	/**
	 * Holds the MySQL database
	 */
	private static String mysql_db;
	
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
	public static void setMySQL(Bot bot, String host, String user, String pass, int port, String db) {
		MySQL.bot = bot;
		MySQL.mysql_host = host;
		MySQL.mysql_user = user;
		MySQL.mysql_pass = pass;
		MySQL.mysql_port = port;
		MySQL.mysql_db = db;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logMessage(LOGLEVEL_CRITICAL, "Could not initialize MySQL Driver!");
			System.exit(-1);
		}
	}

	private static Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:mysql://" + mysql_host + ":"+mysql_port+"/", mysql_user, mysql_pass);
	}

	/**
	 * Gets a ban reason for the specified IP
	 * @param ip String - IP address
	 * @return String - the ban reason
	 */
	public static String getBanReason(String ip) {
		String query = "SELECT `reason` FROM `" + mysql_db + "`.`banlist` WHERE `ip` = ?";
		try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
			pst.setString(1, ip);
			ResultSet r = pst.executeQuery();
			return r.getString("reason");
		}  catch (SQLException e) {
			e.printStackTrace();
			logMessage(LOGLEVEL_IMPORTANT, "Could not check ban.");
			return "null reason";
		}
	}

	/**
	 * Checks if an IP address is banned
	 * @param ip String - ip address
	 * @return true/false
	 */
	public static boolean checkBanned(String ip) {
		String query = "SELECT * FROM `" + mysql_db +"`.`banlist` WHERE `ip` = ?";
		try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
			pst.setString(1, ip);
			ResultSet r = pst.executeQuery();
			if (r.next())
				return true;
			else
				return false;
		}  catch (SQLException e) {
			e.printStackTrace();
			logMessage(LOGLEVEL_IMPORTANT, "Could not check ban.");
			return false;
		}
	}

	/**
	 * Adds a ban to the banlist
	 * @param ip String - ip of the person to ban
	 * @param reason String - the reason to show they are banned for
	 */
	public static void addBan(String ip, String reason) {
		String query = "INSERT INTO `" + mysql_db + "`.`banlist` VALUES (?, ?)";
		try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
			pst.setString(1, ip);
			pst.setString(2, reason);
			if (pst.executeUpdate() == 1)
				bot.sendMessage(bot.cfg_data.irc_channel, "Added ban to banlist.");
			else
				bot.sendMessage(bot.cfg_data.irc_channel, "Could not add ban to banlist.");
		} catch (SQLException e) {
			e.printStackTrace();
			logMessage(LOGLEVEL_IMPORTANT, "Could not add ban to banlist");
		}
	}

	/**
	 * Deletes an IP address from the banlist
	 * @param ip String - the IP address to remove
	 */
	public static void delBan(String ip) {
		String query = "DELETE FROM `" + mysql_db + "`.`banlist` WHERE `ip` = ?";
		try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
			pst.setString(1, ip);
			if (pst.executeUpdate() <= 0)
				bot.sendMessage(bot.cfg_data.irc_channel, "IP does not exist.");
			else {
				// Temporary list to avoid concurrent modification exception
				List<Server> tempList = new LinkedList<>(bot.servers);
				for (Server server : tempList) {
					server.in.println("delban " + ip);
				}
				bot.sendMessage(bot.cfg_data.irc_channel, "Removed " + ip + " from banlist.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logMessage(LOGLEVEL_IMPORTANT, "Could not delete ip from banlist");
		}
	}

	/**
	 * Gets the maximum number of servers the user is allowed to host
	 * @param hostname String - the user's hostname
	 * @return server_limit Int - maximum server limit of the user
	 */
	public static int getMaxSlots(String hostname) {
		if (Functions.checkLoggedIn(hostname)) {
			String query = "SELECT `server_limit` FROM " + mysql_db + ".`login` WHERE `username` = ?";
			try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
				pst.setString(1, Functions.getUserName(hostname));
				ResultSet r = pst.executeQuery();
				if (r.next())
					return r.getInt("server_limit");
				else
					return 0;
			} catch (SQLException e) {
				logMessage(LOGLEVEL_IMPORTANT, "SQL_ERROR in 'getMaxSlots()'");
				e.printStackTrace();
			}
		}
		return AccountType.GUEST; // Return 0, which is a guest and means it was not found; also returns this if not logged in
	}
	
	/**
	 * Queries the database and returns the level of the user
	 * @param hostname of the user
	 * @return level for success, 0 for fail, -1 for non-existent username
	 */
	public static int getLevel(String hostname){
		if (Functions.checkLoggedIn(hostname)) {
			String query = "SELECT `level` FROM " + mysql_db + ".`login` WHERE `username` = ?";
			try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
				pst.setString(1, Functions.getUserName(hostname));
				ResultSet r = pst.executeQuery();
				if (r.next())
					return r.getInt("level");
				else
					return 0;
			} catch (SQLException e) {
				logMessage(LOGLEVEL_IMPORTANT, "SQL_ERROR in 'getLevel()'");
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
	public static void registerAccount(String hostname, String password, String sender) {
		logMessage(LOGLEVEL_NORMAL, "Handling account registration from " + sender + ".");
		// Query to check if the username already exists
		String checkQuery = "SELECT `username` FROM " + mysql_db + ".`login` WHERE `username` = ?";
		
		// Query to add entry to database
		String executeQuery = "INSERT INTO " + mysql_db + ".`login` ( `username`, `password`, `level`, `activated`, `server_limit` ) VALUES ( ?, ?, 1, 1, 4 )";
		try
		(Connection con = getConnection(); PreparedStatement cs = con.prepareStatement(checkQuery); PreparedStatement xs = con.prepareStatement(executeQuery)){
			// Query and check if see if the username exists
			cs.setString(1, Functions.getUserName(hostname));
			ResultSet r = cs.executeQuery();
			
			// The username already exists!
			if (r.next())
				bot.sendMessage(sender, "Account already exists!");
			else {
				// Prepare, bind & execute
				xs.setString(1, Functions.getUserName(hostname));
				// Hash the PW with BCrypt
				xs.setString(2, BCrypt.hashpw(password, BCrypt.gensalt(14)));
				if (xs.executeUpdate() == 1)
					bot.sendMessage(sender, "Account created! Your username is " + Functions.getUserName(hostname) + " and your password is " + password);
				else
					bot.sendMessage(sender, "There was an error registering your account.");
				}
			} catch (SQLException e) {
				logMessage(LOGLEVEL_IMPORTANT, "ERROR: SQL_ERROR in 'registerAccount()'");
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
	public static void changePassword(String hostname, String password, String sender) {
		logMessage(LOGLEVEL_NORMAL, "Password change request from " + sender + ".");
		// Query to check if the username already exists
		String checkQuery = "SELECT `username` FROM " + mysql_db + ".`login` WHERE `username` = ?";
		
		// Query to update password
		String executeQuery = "UPDATE " + mysql_db + ".`login` SET `password` = ? WHERE `username` = ?";
		try
		(Connection con = getConnection(); PreparedStatement cs = con.prepareStatement(checkQuery); PreparedStatement xs = con.prepareStatement(executeQuery)) {
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
			logMessage(LOGLEVEL_IMPORTANT, "SQL_ERROR in 'changePassword()'");
			e.printStackTrace();
			bot.sendMessage(sender, "There was an error changing your password account (thrown SQLException). Try again or contact an administrator with this message.");
		}
	}

	/**
	 * Saves a server host command to a row
	 * @param hostname String - the user's hostname (for verification)
	 * @param words String Array - array of words
	 */
	public static void saveSlot(String hostname, String[] words) {
		if (words.length > 2) {
			String hostmessage = Functions.implode(Arrays.copyOfRange(words, 2, words.length), " ");
			if ((words.length > 2) && (Functions.isNumeric(words[1]))) {
				int slot = Integer.parseInt(words[1]);
				if (slot > 0 && slot < 11) {
					try (Connection con = getConnection()) {
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
						logMessage(LOGLEVEL_IMPORTANT, "SQL Error in 'saveSlot()'");
						e.printStackTrace();
					}
				}
				else
					bot.sendMessage(bot.cfg_data.irc_channel, "You may only specify slot 1 to 10.");
			}
		}
		else
			bot.sendMessage(bot.cfg_data.irc_channel, "Incorrect syntax! Correct usage is .save 1-10 <host_message>");
	}

	/**
	 * Loads server saved with the .save command
	 * @param hostname String - their hostname
	 * @param words String[] - their message
	 * @param level Int - their user level
	 * @param channel String - the channel
	 * @param sender String - sender's name
	 */
	public static void loadSlot(String hostname, String[] words, int level, String channel, String sender) {
		if (words.length == 2) {
			if (Functions.isNumeric(words[1])) {
				int slot = Integer.parseInt(words[1]);
				if (slot > 10 || slot < 1) {
					bot.sendMessage(bot.cfg_data.irc_channel, "Slot must be between 1 and 10.");
					return;
				}
				try (Connection con = getConnection()) {
					String query = "SELECT `serverstring` FROM " + mysql_db + ".`save` WHERE `slot` = ? && `username` = ?";
					PreparedStatement pst = con.prepareStatement(query);
					pst.setInt(1, slot);
					pst.setString(2, Functions.getUserName(hostname));
					ResultSet r = pst.executeQuery();
					if (r.next()) {
						String hostCommand = r.getString("serverstring");
						bot.processHost(level, channel, sender, hostname, hostCommand, false, bot.getMinPort());
					}
					else
						 bot.sendMessage(bot.cfg_data.irc_channel, "You do not have anything saved to that slot!");
				}
				catch (SQLException e) {
					Logger.logMessage(LOGLEVEL_IMPORTANT, "SQL Error in 'loadSlot()'");
					e.printStackTrace();
				}
			}
		}
		else
			bot.sendMessage(bot.cfg_data.irc_channel, "Incorrect syntax! Correct syntax is .load 1 to 10");
	}

	/**
	 * Logs a server to the database
	 * @param servername String - the name of the server
	 * @param unique_id String - the server's unique ID
	 * @param username String - username of server host
	 */
	public static void logServer(String servername, String unique_id, String username) {
		String query = "INSERT INTO `" + mysql_db + "`.`serverlog` (`unique_id`, `servername`, `username`, `date`) VALUES (?, ?, ?, NOW())";
		try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
			pst.setString(1, unique_id);
			pst.setString(2, servername);
			pst.setString(3, username);
			pst.executeUpdate();
			pst.close();
		}
		catch (SQLException e) {
			Logger.logMessage(LOGLEVEL_IMPORTANT, "SQL Exception in logServer()");
			e.printStackTrace();
		}
	}

	/**
	 * Shows a server host string saved with the .save command
	 * @param hostname String - the user's hostname
	 * @param words String[] - array of words of message
	 */
	public static void showSlot(String hostname, String[] words) {
		if (words.length == 2) {
			if (Functions.isNumeric(words[1])) {
				int slot = Integer.parseInt(words[1]);
				if (slot > 0 && slot < 11) {
					String query = "SELECT `serverstring`,`slot` FROM `server`.`save` WHERE `slot` = ? && `username` = ?";
					try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
						pst.setInt(1, slot);
						pst.setString(2, Functions.getUserName(hostname));
						ResultSet rs = pst.executeQuery();
						if (rs.next())
							bot.sendMessage(bot.cfg_data.irc_channel, "In slot " + rs.getString("slot") + ": " + rs.getString("serverstring"));
						else
							bot.sendMessage(bot.cfg_data.irc_channel, "You do not have anything saved to that slot!");
					}
					catch (SQLException e) {
						Logger.logMessage(LOGLEVEL_IMPORTANT, "SQL Error in showSlot()");
						e.printStackTrace();
					}
				}
				else
					bot.sendMessage(bot.cfg_data.irc_channel, "Slot must be between 1 and 10!");
			}
			else
				bot.sendMessage(bot.cfg_data.irc_channel, "Slot must be a number.");
		}
		else
			bot.sendMessage(bot.cfg_data.irc_channel, "Incorrect syntax! Correct usage is .load <slot>");
	}

	/**
	 * Returns a username based on the hostname stored in the database. This is useful for people with custom hostmasks.
	 * @param hostname String - the user's hostname (or hostmask)
	 * @return String - username
	 */
	public static String getUsername(String hostname) {
		String query = "SELECT `username` FROM " + mysql_db + ".`hostmasks` WHERE `hostmask` = ?";
		try (Connection con = getConnection(); PreparedStatement pst = con.prepareStatement(query)) {
			pst.setString(1, hostname);
			ResultSet r = pst.executeQuery();
			if (r.next())
				return r.getString("username");
			else
				return "None";
		}
		catch (SQLException e) {
			Logger.logMessage(LOGLEVEL_IMPORTANT, "SQL Error in getUsername()");
			e.printStackTrace();
			return null;
		}
	}
}
