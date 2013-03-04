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
	 * Holds the MySQL hostname
	 */
	public static String mysqlhost;
	
	/**
	 * Holds the MySQL username
	 */
	public static String mysqluser;
	
	/**
	 * Holds the MySQL password
	 */
	public static String mysqlpass;
	
	/**
	 * Holds the MySQL port
	 */
	public static int mysqlport;
	
	/**
	 * Holds the MySQL database
	 */
	public static String mysqldb;
	
	
	/**
	 * Method to set the MySQL Information
	 * @param host
	 * @param user
	 * @param pass
	 * @param port
	 * @param db
	 */
	public static void setMySQLInformation(String host, String user, String pass, int port, String db) {
		// Sets the MySQL Information with given parameters
		mysqlhost = host;
		mysqluser = user;
		mysqlpass = pass;
		mysqlport = port;
		mysqldb = db;
	}

	/**
	 * Attempts to connect to the database
	 * @return con
	 */
	public static Connection dbConnect() {
		// Connect to the MySQL Databas
        try {
        	Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://" + mysqlhost + ":"+mysqlport+"/", mysqluser, mysqlpass);
			return con;
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("Could not connect to MySQL Database!");
			logMessage("Error connecting to database");
			e.printStackTrace();
		}
        return null;
	}
	
	/**
	 * Queries the database and returns the level of the user
	 * @param hostname
	 * @return level for success, 0 for fail, -1 for non-existant username
	 */
	public static int getLevel(String hostname) {
		if (Functions.checkLoggedIn(hostname)) {
			// Query that gets the level from the passed hostname (user)
			String query = "SELECT `level` FROM " + mysqldb + ".`login` WHERE `username` = ?";	
			try
			(
				Connection con = dbConnect();
				PreparedStatement pst = con.prepareStatement(query);
			){
				// Prepare, bind & execute
				pst.setString(1, Functions.getUserName(hostname));
				ResultSet r = pst.executeQuery();
				
				// Check if resultset returned anything
				if (r.next())
					return r.getInt("level");
				else
					return 0;
			}
			catch (SQLException e) {
				System.out.println("ERROR: SQL_ERROR in 'getLevel()'");
				logMessage("ERROR: SQL_ERROR in 'getLevel()'");
				e.printStackTrace();
			}
		}
		else {
			// Username doesn't exist
			return -1;
		}
		return 0;
	}
	
	/**
	 * Inserts an account into the database
	 * (assuming the user is logged in to IRC)
	 * @param hostname
	 * @param password
	 * @return 1 for success, 0 for fail, -1 account already registered, -2 if not logged in
	 */
	public static int registerAccount(String hostname, String password) {	
		// Query to check if the username already exists
		String checkQuery = "SELECT `username` FROM " + mysqldb + ".`login` WHERE `username` = ?";
		
		// Query to add entry to database
		String executeQuery = "INSERT INTO " + mysqldb + ".`login` ( `username`, `password`, `level`, `activated` ) VALUES ( ?, ?, 0, 1 )";
		try
		(
			Connection con = dbConnect();
			PreparedStatement cs = con.prepareStatement(checkQuery);
			PreparedStatement xs = con.prepareStatement(executeQuery);
		){
			// Query and check if see if the username exists
			cs.setString(1, Functions.getUserName(hostname));
			ResultSet r = cs.executeQuery();
			
			// The username already exists!
			if (r.next())
				return -1;
			
			else {
				// Prepare, bind & execute
				xs.setString(1, r.getString("username"));
				// Hash the PW with BCrypt
				xs.setString(2, BCrypt.hashpw(password, BCrypt.gensalt(14)));
				if (xs.executeUpdate() == 1)
					return 1;
				else
					return 0;
				}
			}
		catch (SQLException e) {
			System.out.println("ERROR: SQL_ERROR in 'registerAccount()'");
			logMessage("ERROR: SQL_ERROR in 'registerAccount()'");
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * Changes the password of a logged in user
	 * (assuming the user is logged into IRC)
	 * @param hostname
	 * @param password
	 * @return -1 username doesn't exist, 0 General problem/SQL error, 1 success
	 */
	public static int changePassword(String hostname, String password) {
		// Query to check if the username already exists
		String checkQuery = "SELECT `username` FROM " + mysqldb + ".`login` WHERE `username` = ?";
		
		// Query to update password
		String executeQuery = "UPDATE " + mysqldb + ".`login` SET `password` = ? WHERE `username` = ?";
		try
		(
			Connection con = dbConnect();
			PreparedStatement cs = con.prepareStatement(checkQuery);
			PreparedStatement xs = con.prepareStatement(executeQuery);
		){
			// Query and check if see if the username exists
			cs.setString(1, Functions.getUserName(hostname));
			ResultSet r = cs.executeQuery();
			
			// The username doesn't exist!
			if (!r.next())
				return -1;
			
			else {
				// Prepare, bind & execute
				xs.setString(1, BCrypt.hashpw(password, BCrypt.gensalt(14)));
				xs.setString(2, r.getString("username"));
				if (xs.executeUpdate() == 1)
					return 1;
				else
					return 0;
				}
			}
		catch (SQLException e) {
			System.out.println("ERROR: SQL_ERROR in 'changePassword()'");
			logMessage("ERROR: SQL_ERROR in 'changePassword()'");
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * This clears the MySQL database at startup so that we can fill it up later
	 * with servers; it is cleared because if there was a shutdown error, the 
	 * database will contain outdated junk
	 */
	public static boolean clearActiveServerList() {
		try (
				Connection con = dbConnect();
				Statement st = con.createStatement();
			){
			st.executeUpdate("TRUNCATE " + mysqldb + ".`active_servers`");
		} catch (SQLException e) {
			e.printStackTrace();
			logMessage("ERROR: SQL_ERROR in 'clearActiveServerList()'");
			return false;
		}
		return true;
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
