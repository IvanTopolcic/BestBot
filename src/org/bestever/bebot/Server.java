package org.bestever.bebot;

import java.util.LinkedList;

public class Server {

	/**
	 * Contains the thread of the server process
	 */
	public ServerProcess serverprocess;
	
	/**
	 * Contains the reference to the bot
	 */
	public Bot bot;
	
	/**
	 * Contains the number of players
	 */
	public byte players = 0;
	
	/**
	 * Contains the port it is run on
	 */
	public int port;
	
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
	public String irc_channel;
	
	/**
	 * This is the host's hostname on irc
	 */
	public String irc_hostname;
	
	/**
	 * This is the login name used
	 */
	public String irc_login;
	
	/**
	 * Contains the entire ".host" command
	 */
	public String host_command;
	
	/**
	 * Contains the hostname used, this will NOT contain " :: [BE] New York "
	 */
	public String servername;

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
	
	/**
	 * Contains the play_time in percentage
	 */
	public long play_time = 0;
	
	/**
	 * Contains the RCON Password
	 */
	public String rcon_password;
	
	/**
	 * If there's an error with processing of numbers, return this
	 */
	public static final int FLAGS_ERROR = 0xFFFFFFFF;
	
	/**
	 * Empty constructor
	 */
	public Server() {
		// Placeholder
	}
	
	/**
	 * This will take ".host ...", parse it and pass it off safely to anything else
	 * that needs the information to create/run the servers and the mysql database.
	 * In addition, all servers will be passed onto a server queue that will use a
	 * thread which processes them one by one from the queue to prevent two servers
	 * attempting to use the same port at the same time
	 * @param botReference The reference to the running bot
	 * @param servers The linkedlist of servers for us to add on a server if successful
	 * @param channel The channel it was sent from
	 * @param sender The sender
	 * @param login The login of the sender
	 * @param hostname The hostname of the sender
	 * @param message The message sent
	 * @return Null if all went well, otherwise an error message to print to the bot
	 */
	public static void handleHostCommand(Bot botReference, LinkedList<Server> servers, String channel, String sender, String login, String hostname, String message) {
		// Initialize server without linking it to the arraylist
		Server server = new Server();
		
		// Reference server to bot
		server.bot = botReference;
		
		// Input basic values
		server.irc_channel = channel;
		server.irc_login = login;
		server.irc_hostname = hostname;
		server.sender = sender;
		server.host_command = message;
		
		// Break up the message, if we have 1 or less keywords then something is wrong 
		String[] keywords = message.split(" ");
		if (keywords.length < 2) {
			server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Not enough parameters");
			return;
		}
		
		// Make sure we have the proper amount of quotation marks (should be even)
		int quotationCounter = 0;
		char[] messageChars = message.toCharArray();
		for (int c = 0; c < messageChars.length; c++)
			if (messageChars[c] == '\"')
				quotationCounter++;
		if (quotationCounter % 2 != 0) {
			server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Invalid amount of quotation marks");
			return;
		}

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
				if (server.compatflags == FLAGS_ERROR) {
					server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Problem with parsing compatflags");
					return;
				}	
				continue;
			}
			
			// compatflags2
			if (keywords[i].toLowerCase().startsWith("compatflags2=")) {
				server.compatflags2 = handleGameFlags(keywords[i]);
				if (server.compatflags2 == FLAGS_ERROR) {
					server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Problem with parsing compatflags2");
					return;
				}
				continue;
			}
			
			// config
			if (keywords[i].toLowerCase().startsWith("config=")) {
				server.config = getDataBetween("config=", message);
			}
			
			// data
			if (keywords[i].toLowerCase().startsWith("data=")) {
				server.disable_skulltag_data = handleDisableSkulltagData(keywords[i]);
				System.out.println("Disable skulltag data = " + server.disable_skulltag_data);
			}
			
			// dmflags
			if (keywords[i].toLowerCase().startsWith("dmflags=")) {
				server.dmflags = handleGameFlags(keywords[i]);
				if (server.dmflags == FLAGS_ERROR) {
					server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Problem with parsing dmflags");
					return;
				}
				continue;
			}
			
			// dmflags2
			if (keywords[i].toLowerCase().startsWith("dmflags2=")) {
				server.dmflags2 = handleGameFlags(keywords[i]);
				if (server.dmflags2 == FLAGS_ERROR) {
					server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Problem with parsing dmflags2");
					return;
				}
				continue;
			}
			
			// dmflags3 
			if (keywords[i].toLowerCase().startsWith("dmflags3=")) {
				server.dmflags3 = handleGameFlags(keywords[i]);
				if (server.dmflags3 == FLAGS_ERROR) {
					server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Problem with parsing dmflags3");
					return;
				}
				continue;
			}
			
			// gamemode
			if (keywords[i].toLowerCase().startsWith("gamemode=")) {
				server.gamemode = getGamemode(keywords[i]);
			}
			
			// hostname (Note: appended the quotation mark for the function)
			if (keywords[i].toLowerCase().startsWith("hostname=\"")) {
				server.servername = getDataBetween("hostname=", message);
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
				server.wads = getDataBetween("wad=", message).replace(',', ' '); // Support for quotation marks only right now, also none for spaces in file names
			}
		}
		
		// Now that we've indexed the string, check to see if we have what we need to start a server
		if (server.iwad == null) {
			server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Incorrect/missing iwad");
			return;
		}
		if (server.gamemode == null) {
			server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Incorrect/missing gamemode");
			return;
		}
		if (server.servername == null) {
			server.bot.sendMessage(server.bot.cfg_data.irc_channel, "Error parsing hostname");
			return;
		}
		
		// Generate the ID [hardcoded banlist, fix in future maybe?]
		server.server_id = Functions.getUniqueID(server.bot.cfg_data.bot_directory_path + "/banlist/");
		
		// Assign and start a new thread
		server.serverprocess = new ServerProcess(server);
		server.serverprocess.start();
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
			case "teamdm":
			case "tdeathmatch":
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

	/**
	 * This handles the skulltag data boolean
	 * @param string The keyword to check
	 * @return True if to use it, false if not
	 */
	private static boolean handleDisableSkulltagData(String string) {
		// Split the string
		String[] value = string.split("=");
		
		// If we don't have exactly 2 values, or the 2nd value is unusual, default to on
		if (value.length != 2 || value[1] == "" || value[1] == null)
			return false;
		
		// If the second keyword matches some known keywords, then disable it
		switch (value[1].toLowerCase()) {
			case "off":
			case "false":
			case "no":
			case "disable":
			case "remove":
				return true;
		}
		
		// Otherwise if something is wrong, just assume we need it
		return false;
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
	 * @param keyword The keyword to check
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
	 * Will return generic things from a server that a user may want to request, this method
	 * does not return anything that contains sensitive information (which can be done with reflection)
	 * @param fieldToGet A String indicating what field to get
	 * @return A String containing the data
	 */
	public String getField(String fieldToGet) {
		switch (fieldToGet.toLowerCase()) {
			case "buckshot":
				return Boolean.toString(this.buckshot);
			case "compatflags":
				return Integer.toString(this.compatflags);
			case "compatflags2":
				return Integer.toString(this.compatflags2);
			case "config":
			case "cfg":
			case "configuration":
				return this.config;
			case "disable_skulltag_data":
			case "stdata":
			case "skulltag_data":
			case "skulltagdata":
				return Boolean.toString(this.disable_skulltag_data);
			case "dmflags":
				return Integer.toString(this.dmflags);
			case "dmflags2":
				return Integer.toString(this.dmflags2);
			case "dmflags3":
				return Integer.toString(this.dmflags3);
			case "gamemode":
			case "gametype":
				return this.gamemode;
			case "host":
			case "hostcommand":
			case "host_command":
				return this.host_command;
			case "instagib":
				return Boolean.toString(this.instagib);
			case "iwad":
				return this.iwad;
			case "mapwad":
			case "mapwads":
				return this.mapwads;
			case "name":
			case "server_name":
			case "hostname":
			case "servername":
				return this.servername;
			case "wad":
			case "wads":
				return this.wads;
			default:
				break;
		}
		return "Error: Not a supported keyword";
	}
	
	/**
	 * Checks to see if the core stuff is needed to start a server
	 * @return True if it's a valid server, false otherwise
	 */
	public boolean validServer() {
		if (this.time_started == 0 || this.play_time == 0)
			return false;
		if (this.sender == null) 
			return false;
		if (this.irc_channel == null) 
			return false;
		if (this.irc_channel == null)
			return false;
		if (this.irc_hostname == null)
			return false;
		if (this.irc_login == null)
			return false;
		if (this.iwad == null)
			return false;
		if (this.gamemode == null)
			return false;
		if (this.rcon_password == null)
			return false;
		if (this.server_id == null)
			return false;
		if (this.servername == null)
			return false;
		return true;
	}
}