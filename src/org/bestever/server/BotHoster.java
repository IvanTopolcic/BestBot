package org.bestever.server;

import org.bestever.exceptions.ConfigException;

public class BotHoster implements Runnable {
	
	/**
	 * The path to the .ini folder.
	 */
	private String iniPath;
	
	/**
	 * When the IRC bot came online.
	 */
	private long irc_hosterbot_starttime;
	
	/**
	 * If the bot hoster is still active.
	 */
	private boolean active;
	
	/**
	 * Loaded INI data.
	 */
	private ServerConfigData cfg_data;
	
	/**
	 * Constructs a basic bothoster object.
	 * @param iniPath The path to the INI to read.
	 */
	public BotHoster(String iniPath) {
		this.iniPath = iniPath;
	}
	
	/**
	 * Gets how long the IRC bot has been operational.
	 * @return A long time in milliseconds from currentTimeMillis().
	 */
	public long getBotStartTime() {
		return irc_hosterbot_starttime;
	}
	
	/**
	 * If the hoster is still active as a thread.
	 * @return True if it is, false otherwise.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Signals to the main thread to quit.
	 */
	public void quit() {
		active = false;
	}

	@Override
	public void run() {
		irc_hosterbot_starttime = System.currentTimeMillis();
		try {
			// Read the config
			cfg_data = new ServerConfigData(iniPath);
			
			// Run the TCP listening thread and server manager
			ServerManager serverManager = new ServerManager();
			ConnectionManager connectionManager = new ConnectionManager(cfg_data.server_listening_port);
			connectionManager.linkToServerManager(serverManager); // Allows server handlers to get incoming connections
			new Thread(serverManager, "ServerManager").start();
			new Thread(connectionManager, "ConnectionManager").start();
		} catch (IndexOutOfBoundsException ie) {
			System.out.println("IndexOutOfBounds: " + ie.getMessage());
			ie.printStackTrace();
		} catch (ConfigException e) {
			System.out.println("Error reading the config: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
