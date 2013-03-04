package org.bestever.bebot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Server implements Serializable {

	////////////
	// Fields //
	////////////
	
	/**
	 * If the server was created successfully and/or is running, this will be true
	 * Set this to transient (no storage in the object file) because it's pointless in there
	 */
	transient public boolean active_server;
	
	/**
	 * Contains the port it is run on
	 */
	public short port;
	
	/**
	 * The time the server was started
	 */
	public long time_started;
	
	/**
	 * This is the generated password at the very start for server logs and the password
	 */
	public String server_id;
	
	/**
	 * Username of the person who sent the command to start it
	 */
	public String sender;
	
	/**
	 * The channel it was hosted from
	 */
	public String channel;
	
	/**
	 * Contains the hostname used, this will NOT contain " :: [BE] New York "
	 */
	public String hostname;
	
	/**
	 * This is the login name used
	 */
	public String login;
	
	/**
	 * Contains the entire ".host" command
	 */
	public String host_command;

	/**
	 * This is the iwad used
	 */
	public String iwad;

	/**
	 * Contains the gamemode
	 */
	public String gamemode;

	/**
	 * The name of the config file (like rofl.cfg), will contain ".cfg" on the end of the string
	 */
	public String config;
	
	/**
	 * Contains a list of all the wads used by the server separated by a space
	 */
	public String wads;

	/**
	 * Contains a list of all the wads separated by a space which will be searched for maps
	 */
	public String mapwads;

	/**
	 * This is for additional instructions the user may specify for one to two things to 
	 * circumvent having to make and upload a whole new config
	 */
	public String additional_instructions;
	
	/**
	 * This contains the actual instructions that the server will run at the very end
	 * Ex: "./zandronum-server -file holycrapbatman_no.wad +duel 1 +customcommandhere"
	 */
	public String server_parameters;
	
	/**
	 * If this is true, that means skulltag data will be enabled
	 */
	public boolean disable_skulltag_data;
	
	/**
	 * If this is true, instagib will be enabled on the server
	 */
	public boolean instagib;
	
	/**
	 * If this is true, buckshot will be enabled on the server
	 */
	public boolean buckshot;

	/**
	 * Contains flags for the server
	 */
	public int dmflags;
	
	/**
	 * Contains flags for the server
	 */
	public int dmflags2;
	
	/**
	 * Contains flags for the server
	 */
	public int dmflags3;
	
	/**
	 * Contains flags for the server
	 */
	public int compatflags;
	
	/**
	 * Contains flags for the server
	 */
	public int compatflags2;
	
	///////////////
	// Constants //
	///////////////
	
	/**
	 *  Random generated ID for serialization
	 */
	private static final long serialVersionUID = -2392019434571282715L;
	
	/**
	 * If there's an error with processing of numbers, return this
	 */
	public static final int FLAGS_ERROR = 0xFFFFFFFF;
	
	/////////////////////////////
	// Method and Constructors //
	/////////////////////////////
	
	/**
	 * Default constructor should be an inactive server
	 */
	public Server() {
		this.active_server = false;
	}
	
	/**
	 * This will take ".host ...", parse it and pass it off safely to anything else
	 * that needs the information to create/run the servers and the mysql database
	 * @param The arraylits of servers for us to add on a server if successful
	 * @param channel The channel it was sent from
	 * @param sender The sender
	 * @param login The login of the sender
	 * @param hostname The hostname of the sender
	 * @param message The message sent
	 * @return Null if all went well, otherwise an error message to print to the bot
	 */
	public static String handleHostCommand(ArrayList<Server> servers, String channel, String sender, String login, String hostname, String message) {
		// Initialize server without linking it to the arraylist
		Server server = new Server();
		
		// Input basic values
		server.channel = channel;
		server.login = login;
		server.hostname = hostname;
		server.sender = sender;
		server.host_command = message;
		
		// Break up the message, if we have 1 or less keywords then something is wrong 
		String[] keywords = message.split(" ");
		if (keywords.length < 2)
			return "Not enough parameters";
		
		// Make sure we have the proper amount of quotation marks (should be even)
		int quotationCounter = 0;
		char[] messageChars = message.toCharArray();
		for (int c = 0; c < messageChars.length; c++)
			if (messageChars[c] == '\"')
				quotationCounter++;
		if (quotationCounter % 2 != 0)
			return "Invalid amount of quotation marks";
		
		// Iterate through every single keyword to construct the host thing except the first index since that's just ".host"
		// For sanity's sake, please keep the keywords in *alphabetical* order
		for (int i = 1; i < keywords.length; i++) {
			// buckshot
			if (keywords[i].toLowerCase().startsWith("buckshot=")) {
				server.buckshot = handleBuckshotOrInstagib(keywords[i]);
			}
			
			// compatflags
			if (keywords[i].toLowerCase().startsWith("compatflags=")) {
				server.compatflags = handleGameFlags(keywords[i]);
				if (server.compatflags == FLAGS_ERROR)
					return "Problem with parsing compatflags";
				continue;
			}
			
			// compatflags2
			if (keywords[i].toLowerCase().startsWith("compatflags2=")) {
				server.compatflags2 = handleGameFlags(keywords[i]);
				if (server.compatflags2 == FLAGS_ERROR)
					return "Problem with parsing compatflags2";
				continue;
			}
			
			// config
			if (keywords[i].toLowerCase().startsWith("config=")) {
				server.config = handleConfig(keywords[i]);
			}
			
			// data
			if (keywords[i].toLowerCase().startsWith("data=")) {
				server.disable_skulltag_data = handleSkulltagData(keywords[i]);
			}
			
			// dmflags
			if (keywords[i].toLowerCase().startsWith("dmflags=")) {
				server.dmflags = handleGameFlags(keywords[i]);
				if (server.dmflags == FLAGS_ERROR)
					return "Problem with parsing dmflags";
				continue;
			}
			
			// dmflags2
			if (keywords[i].toLowerCase().startsWith("dmflags2=")) {
				server.dmflags2 = handleGameFlags(keywords[i]);
				if (server.dmflags2 == FLAGS_ERROR)
					return "Problem with parsing dmflags2";
				continue;
			}
			
			// dmflags3 
			if (keywords[i].toLowerCase().startsWith("dmflags3=")) {
				server.dmflags3 = handleGameFlags(keywords[i]);
				if (server.dmflags3 == FLAGS_ERROR)
					return "Problem with parsing dmflags3";
				continue;
			}
			
			// gamemode
			if (keywords[i].toLowerCase().startsWith("gamemode=")) {
				server.gamemode = getGamemode(keywords[i]);
			}
			
			// hostname (Note: appended the quotation mark for the function)
			if (keywords[i].toLowerCase().startsWith("hostname=\"")) {
				server.hostname = getDataBetween("hostname=", message);
			}
			
			// instagib
			if (keywords[i].toLowerCase().startsWith("instagib=")) {
				server.instagib = handleBuckshotOrInstagib(keywords[i]);
			}
			
			// iwad
			if (keywords[i].toLowerCase().startsWith("iwad=")) {
				server.iwad = getIwad(keywords[i]);
			}
			
			// mapwad (Note: appended the quotation mark for the function)
			if (keywords[i].toLowerCase().startsWith("mapwad=\"")) {
				server.mapwads = getDataBetween("mapwad=", message);
			}
			
			// wad (Note: appended the quotation mark for the function)
			if (keywords[i].toLowerCase().startsWith("wad=\"")) {
				server.wads = getDataBetween("wad=", message);
			}
		}
		
		// Now that we've indexed the string, check to see if we have what we need to start a server
		if (server.iwad == null)
			return "Incorrect/missing iwad";
		if (server.gamemode == null)
			return "Incorrect/missing gamemode";
		if (server.hostname == null)
			return "Error parsing hostname";
		
		// Since all went well, we have to hope the user didn't mess up and proceed to start up the server
		
		
		// Since no errors occured, return a null (meaning no error message)
		return null;
	}
	
	/**
	 * Checks for the iwad based on the input
	 * @param string The keyword with the iwad (ex: iwad=doom2.wad)
	 * @return A string of the wad (lowercase), or null if there's no supported iwad name
	 */
	private static String getIwad(String string) {
		// Split the string
		String[] value = string.split("=");
		
		// If we don't have exactly 2 values, or the 2nd value is unusual, default to on
		if (value.length != 2 || value[1] == "" || value[1] == null)
			return null;
		
		// Check if in array, and if so return that value
		switch (value[1].toLowerCase()) {
			case "doom2":
			case "doom2.wad":
				return "doom2.wad";
			case "doom":
			case "doom.wad":
				return "doom.wad";
			case "tnt":
			case "tnt.wad":
				return "tnt.wad";
			case "plutonia":
			case "plutonia.wad":
				return "plutonia.wad";
			case "heretic":
			case "heretic.wad":
				return "heretic.wad";
			case "hexen":
			case "hexen.wad":
				return "hexen.wad";
			case "strife1":
			case "strife1.wad":
				return "strife1.wad";
			case "sharewaredoom":
			case "doom1":
			case "doom1.wad":
				return "doom1.wad";
			case "hacx":
			case "hacx.wad":
				return "hacx.wad";
			case "chex3":
			case "chex3.wad":
				return "chex3.wad";
			case "megaman":
			case "megagame":
			case "megagame.wad":
				return "megagame.wad";
			case "freedm":
			case "freedm.wad":
				return "freedm.wad";
			case "nerve":
			case "nerve.wad":
				return "nerve.wad";
		}
		
		// If there's no match...
		return null;
	}

	/**
	 * Takes input to parse the gamemode 
	 * @param string The keyword to check with the = sign (ex: gamemode=...)
	 * @return A string of the gamemode, null if there was no such gamemode
	 */
	private static String getGamemode(String string) {
		// Split the string
		String[] value = string.split("=");
		
		// If we don't have exactly 2 values, or the 2nd value is unusual, default to on
		if (value.length != 2 || value[1] == "" || value[1] == null)
			return null;
		
		// Find out if the string we're given matches a game mode
		switch (value[1].toLowerCase())
		{
			case "deathmatch":
			case "dm":
			case "ffa":
				return "deathmatch";
			case "ctf":
				return "ctf";
			case "tdm":
			case "teamdeathmatch":
				return "teamplay";
			case "terminator":
				return "terminator";
			case "possession":
				return "possession";
			case "teampossession":
				return "teampossession";
			case "lms":
			case "lastmanstanding":
				return "lastmanstanding";
			case "tlms":
			case "teamlastmanstanding":
				return "teamlastmanstanding";
			case "skulltag":
				return "skulltag";
			case "duel":
				return "duel";
			case "teamgame":
				return "teamgame";
			case "domination":
				return "domination";
			case "coop":
			case "co-op":
			case "cooperative":
				return "cooperative";
			case "survival":
				return "survival";
			case "invasion":
				return "invasion";
		}
		
		// If the gametype is unknown, return null
		return null;
	}

	// ** TO BE DONE **
	private static String handleConfig(String string) {
		return null;
	}

	/**
	 * This handles the skulltag data boolean
	 * @param string The keyword to check
	 * @return True if to use it, false if not
	 */
	private static boolean handleSkulltagData(String string) {
		// Split the string
		String[] value = string.split("=");
		
		// If we don't have exactly 2 values, or the 2nd value is unusual, default to on
		if (value.length != 2 || value[1] == "" || value[1] == null)
			return true;
		
		// If the second keyword matches some known keywords, then disable it
		switch (value[1].toLowerCase()) {
			case "off":
			case "false":
			case "no":
			case "disable":
			case "remove":
				return false;
		}
		
		// Otherwise if something is wrong, just assume we need it
		return true;
	}
	
	/**
	 * This handles the game modes with default to off
	 * @param string The keyword to check
	 * @return True if to use it, false if not
	 */
	private static boolean handleBuckshotOrInstagib(String string) {
		// Split the string
		String[] value = string.split("=");
		
		// If we don't have exactly 2 values, or the 2nd value is unusual, default to on
		if (value.length != 2 || value[1] == "" || value[1] == null)
			return false;
		
		// If the second keyword matches some known keywords, then disable it
		switch (value[1].toLowerCase()) {
			case "on":
			case "true":
			case "yes":
			case "enable":
				return true;
		}
		
		// Otherwise if something is wrong, just assume we need it
		return false;
	}

	/**
	 * This handles dmflags/compatflags, returns 0xFFFFFFFF if there's an error (FLAGS_ERROR)
	 * @param string The keyword to check
	 * @return A number of what it is
	 */
	private static int handleGameFlags(String keyword) {
		// Split it by the equals sign
		String[] data = keyword.split("=");
		
		// There should only be two parts, the left and the right side
		if (data.length != 2)
			return FLAGS_ERROR;
		
		// If the right side is numeric and passes some logic checks, return that as the flag
		int flag = 0;
		if (Functions.isNumeric(data[1]))
			flag = Integer.parseInt(data[1]);
		if (flag >= 0)
			return flag;
		
		// If something went wrong, return an error
		return FLAGS_ERROR;
	}
	
	/**
	 * This will take your keyword and the main message, and between the two quotation marks it will find 
	 * @param firstKeyword The keyword before the quotation mark (ex: hostname=)
	 * @param fullMessage The entire sent message from the user
	 * @return The string between the two quotes, or null if something went wrong
	 */
	private static String getDataBetween(String firstKeyword, String fullMessage) {
		// Find out the starting place for the keyword
		int startIndex = fullMessage.indexOf(firstKeyword);
		
		// If the keyword isn't in the message or is at the very end, it doesn't exist/is messed up
		if (startIndex == -1 || startIndex + firstKeyword.length() >= fullMessage.length())
			return null;
		
		// Now make startIndex start from the quotation mark
		startIndex = startIndex + firstKeyword.length() + 1;
		
		// Start looking from (start index + word length + 1 char for the quotation mark) to the end
		int endIndex = -1;
		char[] fullMessageChars = fullMessage.toCharArray();
		for (int c = startIndex; c < fullMessageChars.length; c++) {
			if (fullMessageChars[c] == '"') {
				endIndex = c;
				break;
			}
		}
		
		// If something goes wrong, return null
		if (endIndex == -1 || endIndex - startIndex <= 0)
			return null;
		
		// Set up the return string, handle possible errors
		String returnString = null;
		try {
			returnString = fullMessage.substring(startIndex, endIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return returnString; 
	}
	
	/**
	 * This method serializes a given server to a folder specified (probably in the .ini) so
	 * that servers can be re-initialized at some point in the future
	 * @param server The server object to serialize
	 * @param folderPath The path to the folder where Server objects are writen/read from
	 * @return True if successful in writing the object, false if not
	 */
	public static boolean serializeServer(Server server, String folderPath, String extension) {
		
		// Make sure the server and folderPath are valid
		if (server == null)
			return false;
		
		// Make sure the server is actually in some kind of working function
		if (!server.active_server)
			return false;
		
		// Set our file up
		File objectFile = new File(folderPath + Integer.toString(server.port) + extension);
		
		// If the file doesnt exist, create it
		if (!objectFile.exists()) {
			try {
				objectFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		// If we can't write to it, or something is wrong, return false
		if (!objectFile.canRead() || !objectFile.canWrite())
			return false;
	
		// Prepare object output stream for writing
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(objectFile.getPath())));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		// Since our objectstream should be functional, write the server
		try {
			oos.writeObject(server);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		// Close
		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		// Verify if the final exists
		return true;
	}
}