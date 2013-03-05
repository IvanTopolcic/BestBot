package org.bestever.bebot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
	 * This is for initialization being indicated as complete
	 */
	private boolean initialized;
	
	/**
	 * This should be called before starting run
	 * @param serverReference A reference to the server it is connected to (establishing a back/forth relationship to access its data)
	 */
	public ServerProcess(Server serverReference) {
		this.server = serverReference;
		this.serverRunCommand = processServerRunCommand();
		this.initialized = (this.server != null && this.serverRunCommand != null);
	}
	
	/**
	 * The Server object is taken and
	 * @return The hostbuilder string based on the data in the Server object
	 */
	private String processServerRunCommand() {
		// This shouldn't happen but you never know
		if (server == null)
			return null;
		
		String runCommand = "zandronum";
		
		if (server.iwad != null)
			runCommand += " -iwad " + server.iwad;
		
		// If we have either wads or skulltag_data, then prepare files
		if (server.wads != null || !server.disable_skulltag_data) {
			runCommand += " -file ";
			if (!server.disable_skulltag_data)
				runCommand += "skulltag_data.pk3 skulltag_actors.pk3 ";
			if (server.wads != null)
				runCommand += server.wads; // No space needed, taken care of earlier
		}
		
		if (server.gamemode != null)
			runCommand += " +" + server.gamemode + " 1";
		
		if (server.config != null)
			runCommand += " +" + server.config;
		
		/*
				-iwad file
				+exec [configdir/config].cfg
			+addmap map
				-file [skulltag_data.pk3 skulltag_actors.pk3] wad
				+[gamemode] 1
			+sv_hostname [name]
			+[dmflags] #
			-port #
			+sv_rconpassword id
			+sv_banfile banlist/[id].txt
			+sv_adminlistfile adminlist/[id].txt
			+sv_banexemptionfile whitelist/[id].txt
		 */
		
		return runCommand;
	}
	
	@Override
	public void run() {
		// If we have not initialized the process, do not set up a server (to prevent errors)
		if (!initialized)
			return;
		
		// Set up the server
		ProcessBuilder pb = new ProcessBuilder(serverRunCommand);
		pb.redirectErrorStream(true);
		try
		{		
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

			//BotRun.sendMessage(server.sender, "Your unique server ID is: " + server.server_id + ". This is your RCON password, which can be used using send_password. You can view your server's logfile at http://www.best-ever.org/logs/" + ID + ".txt");

			long start = System.nanoTime();
			
			while ((strLine = br.readLine()) != null) {
				if (strLine.equalsIgnoreCase("UDP Initialized.")) {
					//BotRun.sendMessage(server.channel, "Server started successfully.");
					//BotRun.sendMessage(server.sender, "To kill your server, type .killmine (this will kill all of your servers), or .kill " + port);
				}
				
				Calendar currentDate = Calendar.getInstance();
				dateNow = formatter.format(currentDate.getTime());

				file.write(dateNow + " " + strLine + "\n");
				file.flush();
			}
			
			Calendar currentDate = Calendar.getInstance();
			dateNow = formatter.format(currentDate.getTime());
			long end = System.nanoTime();
			long uptime = end - start;
			file.write(dateNow + " Server stopped! Uptime was " + Functions.calculateTime(uptime / 1000000000));
			file.close();
			fstream.close();
			//BotRun.sendMessage(server.channel, "Server stopped on port " + server.port +"! Server ran for " + Functions.calculateTime(uptime / 1000000000));

			Thread.currentThread().interrupt();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}