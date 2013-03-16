package org.bestever.bebot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ListIterator;

/**
 * This class is specifically for running the server only and notifying the 
 * bot when the server is closed, or when to be terminated; nothing more
 */
public class ServerProcess extends Thread {

	/**
	 * This contains the strings that will run in the process builder
	 */
	private String serverRunCommands;
	
	/**
	 * A reference to the server
	 */
	private Server server;
	
	/**
	 * The process of the server
	 */
	private Process proc;
	
	/**
	 * The basic log writer to be used by bufferedLogWriter
	 */
	FileWriter logWriter;
	
	/**
	 * Holds the buffered log writer to entering text into the log
	 */
	BufferedWriter bufferedLogWriter;
	
	
	/**
	 * This should be called before starting run
	 * @param serverReference A reference to the server it is connected to (establishing a back/forth relationship to access its data)
	 */
	public ServerProcess(Server serverReference) {
		this.server = serverReference;
		this.serverRunCommands = processServerRunCommand();
	}
	
	/** 
	 * Is used to indicate if the ServerProcess was initialized properly
	 * @return True if it was initialized properly, false if something went wrong
	 */
	private boolean isInitialized() {
		return this.server != null && this.serverRunCommands != null;
	}
	
	/** 
	 * This method can be invoked to signal the thread to kill itself and the process
	 */
	public void terminateServer() {
		proc.destroy();
	}
	
	/**
	 * The Server object is taken and
	 * @return The hostbuilder string based on the data in the Server object
	 */
	private String processServerRunCommand() {
		// This shouldn't happen but you never know
		if (server == null)
			return null;
		
		// Create an arraylist with all our strings
		ArrayList<String> runCommand = new ArrayList<>();
		runCommand.add(server.bot.cfg_data.bot_executable); // This must always be first
		runCommand.add("-port " + Integer.toString(server.bot.cfg_data.bot_min_port)); // Always start on the minimum port and let zandronum handle the rest
		
		if (server.iwad != null)
			runCommand.add("-iwad iwads/" + server.iwad);
		
		if (!server.disable_skulltag_data)
			runCommand.add("-file skulltag_data.pk3 skulltag_actors.pk3");
		
		if (server.wads != null)
			runCommand.add("-file \"server.wads\"");
		
		if (server.config != null)
			runCommand.add("+exec \"" + server.config + "\"");
		
		if (server.gamemode != null)
			runCommand.add("+" + server.gamemode + " 1");
		
		if (server.dmflags > 0)
			runCommand.add("+dmflags " + Integer.toString(server.dmflags));
		
		if (server.dmflags2 > 0)
			runCommand.add("+dmflags2 " + Integer.toString(server.dmflags2));
		
		if (server.dmflags3 > 0)
			runCommand.add("+dmflags3 " + Integer.toString(server.dmflags3));
		
		if (server.compatflags > 0)
			runCommand.add("+compatflags " + Integer.toString(server.compatflags));
		
		if (server.compatflags2 > 0)
			runCommand.add("+compatflags2 " + Integer.toString(server.compatflags2));
		
		if (server.instagib)
			runCommand.add("+instagib 1");
		
		if (server.buckshot)
			runCommand.add("+buckshot 1");
		
		if (server.hostname != null)
			runCommand.add("+sv_hostname \"" + server.sv_hostname + "\"");
		
		// These must be added; could be extended by config; these are hardcoded for now
		runCommand.add("+sv_rconpassword " + server.server_id);
		//runCommand += " +sv_banfile banlist/" + server.server_id + ".txt";
		//runCommand += " +sv_adminlistfile adminlist/" + server.server_id + ".txt";
		//runCommand += " +sv_banexemptionfile whitelist/" + server.server_id + ".txt";
		
		//+addmap map from mapwad goes here
		
		String execCommand = "";
		ListIterator<String> it = runCommand.listIterator();
		while (it.hasNext())
			if (it.nextIndex() != 0)
				execCommand += " " + it.next();
			else
				execCommand += it.next();
		return execCommand;
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
		File logFile = null;
		long start = System.nanoTime();
		String strLine = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
		String dateNow = "";
		try {
			// Set up the server
			System.out.println("Running process: " + serverRunCommands);
			proc = Runtime.getRuntime().exec(serverRunCommands);
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			// Set up file/IO
			logFile = new File("/home/auto/skulltag/public_html/logs/" + server.server_id + ".txt");
			logWriter = new FileWriter("/home/auto/skulltag/public_html/logs/" + server.server_id + ".txt");
			bufferedLogWriter = new BufferedWriter(logWriter);
			
			// Write header in the file
			if (!logFile.exists())
				logFile.createNewFile();
			bufferedLogWriter.write("_______               __           _______                     \n");
			bufferedLogWriter.write("|   __ \\.-----.-----.|  |_        |    ___|.--.--.-----.----.  \n");
			bufferedLogWriter.write("|   __ <|  -__|__ --||   _|__     |    ___||  |  |  -__|   _|_ \n");
			bufferedLogWriter.write("|______/|_____|_____||____|__|    |_______| \\___/|_____|__||__|\n");
			bufferedLogWriter.write("________________________________________________________________________________________________________\n\n");
			bufferedLogWriter.flush();
			server.bot.sendMessage(server.sender, "Your unique server ID is: " + server.server_id + ". This is your RCON password, which can be used using send_password. You can view your server's logfile at http://www.best-ever.org/logs/" + server.server_id + ".txt");
			
			// Process server while it outputs text
			while ((strLine = br.readLine()) != null) {
				// Make sure to get the port [Server using alternate port 10666.]
				if (strLine.startsWith("Server using alternate port ")) {
					System.out.println(strLine);
					portNumber = strLine.replace("Server using alternate port ", "").replace(".", "").trim();
					if (Functions.isNumeric(portNumber)) {
						server.port = Integer.parseInt(portNumber);
						server.bot.sendMessage(server.channel, "First port set to: " + server.port);
					} else
						server.bot.sendMessage(server.channel, "Warning: port parsing error when setting up server [1]; contact an administrator.");
					
				// If the port is used [NETWORK_Construct: Couldn't bind to 10666. Binding to 10667 instead...]
				} else if (strLine.startsWith("NETWORK_Construct: Couldn't bind to ")) {
					System.out.println(strLine);
					portNumber = strLine.replace(new String("NETWORK_Construct: Couldn't bind to " + portNumber + ". Binding to "), "").replace(" instead...", "").trim();
					if (Functions.isNumeric(portNumber)) {
						server.port = Integer.parseInt(portNumber);
						server.bot.sendMessage(server.channel, "Last port set to: " + server.port);
					} else
						server.bot.sendMessage(server.channel, "Warning: port parsing error when setting up server [2]; contact an administrator.");
				}
				
				// If we see this, the server started
				if (strLine.equalsIgnoreCase("UDP Initialized.")) {
					System.out.println(strLine);
					server.bot.servers.add(server); // Add the server to the linked list since it's fully operational now
					server.bot.sendMessage(server.channel, "Server started successfully.");
					server.bot.sendMessage(server.sender, "To kill your server, type .killmine (this will kill all of your servers), or .kill " + server.port);
				}
				
				if (!strLine.startsWith("CHAT")) {
					if (strLine.endsWith("has connected."))
						server.players += 1;
					else if (strLine.endsWith("disconnected."))
						server.players -= 1;
				}
				
				if (strLine.startsWith("-> map")) {
					server.players = 0;
				}
				
				dateNow = formatter.format(Calendar.getInstance().getTime());
				bufferedLogWriter.write(dateNow + " " + strLine + "\n");
				bufferedLogWriter.flush();
			}
			
			// Handle cleanup
			dateNow = formatter.format(Calendar.getInstance().getTime());
			long end = System.nanoTime();
			long uptime = end - start;
			bufferedLogWriter.write(dateNow + " Server stopped! Uptime was " + Functions.calculateTime(uptime / 1000000000));
			
			// Notify the main channel
			server.bot.sendMessage(server.channel, "Server stopped on port " + server.port +"! Server ran for " + Functions.calculateTime(uptime / 1000000000));

			// Remove the server from the linked list
			System.out.println("Removing server status: " + server.bot.servers.remove(server));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedLogWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				logWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}