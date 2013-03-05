package org.bestever.bebot;

/*
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
*/

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
		
		String runCommand = server.bot.cfg_data.bot_executable;
		
		runCommand += " -port " + server.port;
		
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
			+addmap map from mapwad
		 */
		
		return runCommand;
	}
	
	@Override
	public void run() {
		// If we have not initialized the process, do not set up a server (to prevent errors)
		if (!initialized)
			return;
		
		server.bot.sendMessage(server.channel, this.serverRunCommand);
		
		/*
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

			server.bot.sendMessage(server.sender, "Your unique server ID is: " + server.server_id + ". This is your RCON password, which can be used using send_password. You can view your server's logfile at http://www.best-ever.org/logs/" + server.server_id + ".txt");

			long start = System.nanoTime();
			
			while ((strLine = br.readLine()) != null) {
				if (strLine.equalsIgnoreCase("UDP Initialized.")) {
					server.bot.sendMessage(server.channel, "Server started successfully.");
					server.bot.sendMessage(server.sender, "To kill your server, type .killmine (this will kill all of your servers), or .kill " + server.port);
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
			server.bot.sendMessage(server.channel, "Server stopped on port " + server.port +"! Server ran for " + Functions.calculateTime(uptime / 1000000000));

			Thread.currentThread().interrupt();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
}