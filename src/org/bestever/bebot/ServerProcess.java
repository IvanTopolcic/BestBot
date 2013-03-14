package org.bestever.bebot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class is specifically for running the server only and notifying the 
 * bot when the server is closed, or when to be terminated; nothing more
 */
public class ServerProcess implements Runnable {

	/**
	 * This contains the strings that will run in the process builder
	 */
	private String serverRunCommand;
	
	/**
	 * A reference to the server
	 */
	private Server server;
	
	/**
	 * This should be called before starting run
	 * @param serverReference A reference to the server it is connected to (establishing a back/forth relationship to access its data)
	 */
	public ServerProcess(Server serverReference) {
		this.server = serverReference;
		this.serverRunCommand = processServerRunCommand();
	}
	
	/** 
	 * Is used to indicate if the ServerProcess was initialized properly
	 * @return True if it was initialized properly, false if something went wrong
	 */
	private boolean isInitialized() {
		return this.server != null && this.serverRunCommand != null;
	}
	
	/**
	 * The Server object is taken and
	 * @return The hostbuilder string based on the data in the Server object
	 */
	private String processServerRunCommand() {
		// This shouldn't happen but you never know
		if (server == null)
			return null;
		
		String runCommand = server.bot.cfg_data.bot_executable;
		
		runCommand += " -port " + Integer.toString(server.bot.cfg_data.bot_min_port); // Always start on the minimum port and let zandronum handle the rest
		
		if (server.iwad != null)
			runCommand += " -iwad " + server.iwad;
		
		// If we have either wads or skulltag_data, then prepare files
		if (server.wads != null || !server.disable_skulltag_data) {
			runCommand += " -file ";
			
			if (!server.disable_skulltag_data)
				runCommand += "skulltag_data.pk3 skulltag_actors.pk3";
			
			if (server.wads != null && !server.disable_skulltag_data)
				runCommand += " " + server.wads; // If we added skulltag data from before, then start with a space
			else if (server.wads != null)
				runCommand += server.wads; // Otherwise if we didn't add st data before, there is already a space so don't add one
		}
		
		if (server.config != null)
			runCommand += " +exec \"" + server.config + "\"";
		
		if (server.gamemode != null)
			runCommand += " +" + server.gamemode + " 1";
		
		if (server.dmflags > 0)
			runCommand += " +dmflags " + server.dmflags;
		
		if (server.dmflags2 > 0)
			runCommand += " +dmflags2 " + server.dmflags2;
		
		if (server.dmflags3 > 0)
			runCommand += " +dmflags3 " + server.dmflags3;
		
		if (server.compatflags > 0)
			runCommand += " +compatflags " + server.compatflags;
		
		if (server.compatflags2 > 0)
			runCommand += " +compatflags2 " + server.compatflags2;
		
		if (server.instagib)
			runCommand += " +instagib 1";
		
		if (server.buckshot)
			runCommand += " +buckshot 1";
		
		if (server.hostname != null)
			runCommand += " +sv_hostname \"" + server.sv_hostname + "\"";
		
		// These must be added; could be extended by config; these are hardcoded for now
		runCommand += " +sv_rconpassword " + server.server_id;
		runCommand += " +sv_banfile banlist/" + server.server_id + ".txt";
		runCommand += " +sv_adminlistfile adminlist/" + server.server_id + ".txt";
		runCommand += " +sv_banexemptionfile whitelist/" + server.server_id + ".txt";
		
		/*
			+addmap map from mapwad goes here
		 */
		
		return runCommand;
	}
	
	@Override
	public void run() {
		// If we have not initialized the process, do not set up a server (to prevent errors)
		if (!isInitialized()) {
			server.bot.sendMessage(server.channel, "Warning: Initialization error for server thread; please contact an administrator.");
			return;
		}
		
		// Attempt to start up the server
		String portNumber = ""; // This will hold the port number
		try {
			server.bot.sendMessage(server.channel, this.serverRunCommand); // DEBUG
			// Set up the server
			ProcessBuilder pb = new ProcessBuilder(serverRunCommand);
			pb.redirectErrorStream(true);		
			Process proc = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String strLine = null;
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
			String dateNow = "";
			FileWriter fstream = new FileWriter("/home/auto/skulltag/public_html/logs/" + server.server_id + ".txt");
			BufferedWriter file = new BufferedWriter(fstream);
			file.write("_______               __           _______                     \n");
			file.write("|   __ \\.-----.-----.|  |_        |    ___|.--.--.-----.----.  \n");
			file.write("|   __ <|  -__|__ --||   _|__     |    ___||  |  |  -__|   _|_ \n");
			file.write("|______/|_____|_____||____|__|    |_______| \\___/|_____|__||__|\n");
			file.write("________________________________________________________________________________________________________\n\n");
			file.flush();
			server.bot.sendMessage(server.sender, "Your unique server ID is: " + server.server_id + ". This is your RCON password, which can be used using send_password. You can view your server's logfile at http://www.best-ever.org/logs/" + server.server_id + ".txt");
			long start = System.nanoTime();
			Calendar currentDate;
			
			// Handle the output of the server
			while ((strLine = br.readLine()) != null) {
				// Make sure to get the port [Server using alternate port 10666.]
				if (strLine.startsWith("Server using alternate port ")) {
					portNumber = strLine.replace("Server using alternate port ", "").replace(".", "").trim();
					if (Functions.isNumeric(portNumber))
						server.port = Integer.parseInt(portNumber);
					else
						server.bot.sendMessage(server.channel, "Warning: port parsing error when setting up server [1]; contact an administrator.");
				// If the port is used [NETWORK_Construct: Couldn't bind to 10666. Binding to 10667 instead...]
				} else if (strLine.startsWith("NETWORK_Construct: Couldn't bind to ")) {
					portNumber = strLine.replace(new String("NETWORK_Construct: Couldn't bind to " + portNumber + ". Binding to "), "").replace(" instead...", "").trim();
					if (Functions.isNumeric(portNumber))
						server.port = Integer.parseInt(portNumber);
					else
						server.bot.sendMessage(server.channel, "Warning: port parsing error when setting up server [2]; contact an administrator.");
				}
				
				// If we see this, the server started
				if (strLine.equalsIgnoreCase("UDP Initialized.")) {
					server.bot.servers.add(server); // Add the server to the linked list since it's fully operational now
					server.bot.sendMessage(server.channel, "Server started successfully.");
					server.bot.sendMessage(server.sender, "To kill your server, type .killmine (this will kill all of your servers), or .kill " + server.port);
				}
				
				currentDate = Calendar.getInstance();
				dateNow = formatter.format(currentDate.getTime());
				file.write(dateNow + " " + strLine + "\n");
				file.flush();
			}
			
			currentDate = Calendar.getInstance();
			dateNow = formatter.format(currentDate.getTime());
			long end = System.nanoTime();
			long uptime = end - start;
			file.write(dateNow + " Server stopped! Uptime was " + Functions.calculateTime(uptime / 1000000000));
			file.close();
			fstream.close();
			server.bot.sendMessage(server.channel, "Server stopped on port " + server.port +"! Server ran for " + Functions.calculateTime(uptime / 1000000000));
			//Thread.currentThread().interrupt(); // Is this needed?
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}