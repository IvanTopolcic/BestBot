package org.bestever.bebot;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.bestever.bebot.Logger.logMessage;

/**
 * MySQL Class for handling all of the database inserts/fetching
 */
public class MySQL {
	
	/**
	 * Holds the Connection
	 */
	public Connection con;
	
	/**
	 * Holds the Bot
	 */
	public Bot bot;
	
	/**
	 * Holds the MySQL hostname
	 */
	public String mysql_host;
	
	/**
	 * Holds the MySQL username
	 */
	public String mysql_user;
	
	/**
	 * Holds the MySQL password
	 */
	public String mysql_pass;
	
	/**
	 * Holds the MySQL port
	 */
	public int mysql_port;
	
	/**
	 * Holds the MySQL database
	 */
	public String mysql_db;
	
	/**
	 * Constructor for the MySQL Object
	 * @param bot
	 * @param host
	 * @param user
	 * @param pass
	 * @param port
	 * @param db
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
			Connection connection = DriverManager.getConnection("jdbc:mysql://" + mysql_host + ":"+mysql_port+"/", mysql_user, mysql_pass);
			this.con = connection;
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("Could not connect to MySQL Database!");
			logMessage("Error connecting to database");
			e.printStackTrace();
		}
	}
	
	/**
	 * Queries the database and returns the level of the user
	 * @param hostname
	 * @return level for success, 0 for fail, -1 for non-existant username
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public int getLevel(String hostname) {
		if (Functions.checkLoggedIn(hostname)) {
			String query = "SELECT `level` FROM " + mysql_db + ".`login` WHERE `username` = ?";
			try ( PreparedStatement pst = con.prepareStatement(query); ) {
				// Prepare, bind & execute
				pst.setString(1, Functions.getUserName(hostname));
				ResultSet r = pst.executeQuery();
				
				// Check if resultset returned anything
				if (r.next())
					return r.getInt("level");
				else
					return 0;
			} catch (SQLException e) {
				System.out.println("ERROR: SQL_ERROR in 'getLevel()'");
				logMessage("ERROR: SQL_ERROR in 'getLevel()'");
				e.printStackTrace();
			}
		}
		return AccountType.GUEST; // Return 0, which is a guest and means it was not found; also returns this if not logged in
	}
	
	/**
	 * Inserts an account into the database
	 * (assuming the user is logged in to IRC)
	 * @param hostname
	 * @param password
	 * @return 1 for success, 0 for fail, -1 account already registered, -2 if not logged in
	 */
	public void registerAccount(String hostname, String password, String sender) {	
		// Query to check if the username already exists
		String checkQuery = "SELECT `username` FROM " + mysql_db + ".`login` WHERE `username` = ?";
		
		// Query to add entry to database
		String executeQuery = "INSERT INTO " + mysql_db + ".`login` ( `username`, `password`, `level`, `activated` ) VALUES ( ?, ?, 0, 1 )";
		try
		(
			PreparedStatement cs = con.prepareStatement(checkQuery);
			PreparedStatement xs = con.prepareStatement(executeQuery);
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
			logMessage("ERROR: SQL_ERROR in 'registerAccount()'");
			e.printStackTrace();
			bot.sendMessage(sender, "There was an error registering your account.");
		}
	}
	
	/**
	 * Changes the password of a logged in user
	 * (assuming the user is logged into IRC)
	 * @param hostname
	 * @param password
	 * @return -1 username doesn't exist, 0 General problem/SQL error, 1 success
	 */
	public void changePassword(String hostname, String password, String sender) {
		// Query to check if the username already exists
		String checkQuery = "SELECT `username` FROM " + mysql_db + ".`login` WHERE `username` = ?";
		
		// Query to update password
		String executeQuery = "UPDATE " + mysql_db + ".`login` SET `password` = ? WHERE `username` = ?";
		try
		(
			PreparedStatement cs = con.prepareStatement(checkQuery);
			PreparedStatement xs = con.prepareStatement(executeQuery);
		){
			// Query and check if see if the username exists
			cs.setString(1, Functions.getUserName(hostname));
			ResultSet r = cs.executeQuery();
			
			// The username doesn't exist!
			if (!r.next())
				bot.sendMessage(bot.cfg_data.irc_channel, "Username does not exist.");
			
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
			logMessage("ERROR: SQL_ERROR in 'changePassword()'");
			e.printStackTrace();
			bot.sendMessage(sender, "There was an error changing your password account (thrown SQLException). Try again or contact an administrator with this message.");
		}
	}
	
	/**
	 * This clears the MySQL database at startup so that we can fill it up later
	 * with servers; it is cleared because if there was a shutdown error, the 
	 * database will contain outdated junk
	 */
	public void clearActiveServerList() {
		try (
				Statement st = con.createStatement();
			){
			st.executeUpdate("TRUNCATE " + mysql_db + ".`active_servers`");
		} catch (SQLException e) {
			e.printStackTrace();
			logMessage("ERROR: SQL_ERROR in 'clearActiveServerList()'");
		}
	}
	
	/**
	 * This is invoked to request mysql to add the server object to the database
	 * How it should be handled is all the fields in the Server class will be entered
	 * into the database
	 * @param server The Server object to add to the database
	 * @return An integer constant stating success/failure/other (maybe convert to boolean later if theres only 2 return codes)
	 */
	public static int addServerToDatabase(Server server) {
		return 0;
	}
}
