package org.bestever.bebot;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.bestever.bebot.Logger.logMessage;

/**
 * MySQL Class for handling all of the database inserts/fetching
 */
public class MySQL {
	
	/**
	 * MySQL Connection object
	 */
	public static Connection con;
	
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
	 * Method to set the MySQL Information
	 * @param host
	 * @param user
	 * @param pass
	 * @param port
	 */
	public static void setMySQLInformation(String host, String user, String pass, int port) {
		
		// Sets the MySQL Information with given parameters
		mysqlhost = host;
		mysqluser = user;
		mysqlpass = pass;
		mysqlport = port;
	}

	/**
	 * Attempts to connect to the database
	 * @return con
	 */
	public static Connection dbConnect() {
		
		// Connect to the MySQL Databas
        try {
        	Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://" + mysqlhost + ":"+mysqlport+"/", mysqluser, mysqlpass);
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("Could not connect to MySQL Database!");
			logMessage("Error connecting to database");
			e.printStackTrace();
		}
        return con;
	}
	
	/**
	 * Queries the database and returns the level of the user
	 * @param hostname
	 * @return level for success, 0 for fail, -1 for non-existant username
	 */
	public static int getLevel(String hostname) {
		if (Functions.checkLoggedIn(hostname)) {
			
			// Query that gets the level from the passed hostname (user)
			String query = "SELECT `level` FROM `server`.`login` WHERE `username` = ?";	
			
			try
			(
				Connection con = dbConnect();
				PreparedStatement pst = con.prepareStatement(query);
			)
			{

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
		// Using two queries is probably bad, but using one will require modification to the database
		// Maybe at a later time...
		String checkQuery = "SELECT `username` FROM `server`.`login` WHERE `username` = ?";
		
		// Query to add entry to database
		String executeQuery = "INSERT INTO `server`.`login` ( `username`, `password`, `level`, `activated` ) VALUES ( ?, ?, 0, 1 )";
		
		// Try-with-resources that we do not need to close
		try
		(
			Connection con = dbConnect();
			PreparedStatement cs = con.prepareStatement(checkQuery);
			PreparedStatement xs = con.prepareStatement(executeQuery);
		)
		{
			
			// Query and check if see if the username exists
			cs.setString(1, Functions.getUserName(hostname));
			ResultSet r = cs.executeQuery();
			
			// The username already exists!
			if (r.next())
				return -1;
			
			else
			{
				// Prepare, bind & execute
				xs.setString(1, Functions.getUserName(hostname));
				// Hash the PW with BCrypt
				xs.setString(2, BCrypt.hashpw(password, BCrypt.gensalt(14)));
				int rows_affected = xs.executeUpdate();
				if (rows_affected == 1)
					return 1;
				else
					return 0;
				}
			}
		catch (SQLException e) {
			System.out.println("ERROR: SQL_ERROR in 'registerAccount()'");
			logMessage("ERROR: SQL_ERROR in 'registerAccount()'");
			e.printStackTrace();
			return -1;
		}
	}
}
