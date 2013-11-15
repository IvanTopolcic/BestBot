package org.bestever.irc;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.bestever.common.Logger;
import org.bestever.exceptions.ConfigException;
import org.bestever.net.Connection;
import org.bestever.net.MessageReceiver;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

public class BotIRC implements Runnable, MessageReceiver {
	
	/**
	 * The new bot we will use.
	 */
	private PircBotX bot;

	/**
	 * The path to the .ini folder.
	 */
	private String iniPath;
	
	/**
	 * If the IRC bot is active (thread-wise).
	 */
	private boolean active;
	
	/**
	 * When the IRC bot came online.
	 */
	private long irc_bot_starttime;
	
	/**
	 * Loaded INI data.
	 */
	private IRCConfigData cfg_data;
	
	/**
	 * The connection to the server.
	 */
	private Connection connectionToServer;
	
	/**
	 * The last time a ping (ms) was sent from this IRC bot to the server.
	 */
	private long lastPingSent;
	
	/**
	 * A collection of messages for us to deploy to our connection when the
	 * polling begins.
	 */
	private LinkedList<String> messagesToSend;

	/**
	 * Initializes this object so it can be called by run(), which will handle
	 * setting all the other fields.
	 * @param iniPath The path to the INI file. If there is any kind of error,
	 * the bot will terminate itself and report an error.
	 */
	public BotIRC(String iniPath) {
		this.iniPath = iniPath;
		this.messagesToSend = new LinkedList<>();
	}
	
	/**
	 * Gets how long the IRC bot has been operational.
	 * @return A long time in milliseconds from currentTimeMillis().
	 */
	public long getBotStartTime() {
		return irc_bot_starttime;
	}
	
	/**
	 * If the main thread is still running.
	 * @return True if it is, false if it's not.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Sends a message to the main channel.
	 * @param msg The message. There is no null/empty checking on this string,
	 * so it should be done beforehand.
	 */
	public void sendMessageToMainChannel(String msg) {
		if (bot == null)
			return;
		Channel channel = bot.getChannel(cfg_data.irc_channel);
		bot.sendMessage(channel, msg);
	}
	
	/**
	 * Attempts to connect to the hosting server.
	 * @return True if the connection succeeded, false otherwise.
	 */
	private boolean connectToServer() {
		try {
			Socket socket = new Socket(cfg_data.irc_server_ip, cfg_data.irc_server_port);
			connectionToServer = new Connection(socket, this);
		} catch (UnknownHostException e) {
			System.out.println("(UnknownHostException) Unable to connect to " + cfg_data.irc_server_ip + ":" + cfg_data.irc_server_port);
			if (connectionToServer != null) {
				connectionToServer.terminate();
				connectionToServer = null;
			}
			return false;
		} catch (SocketException se) {
			System.out.println("Socket exception when trying to connect to " + cfg_data.irc_server_ip + ":" + cfg_data.irc_server_port);
			if (connectionToServer != null) {
				connectionToServer.terminate();
				connectionToServer = null;
			}
			return false;
		} catch (IOException e) {
			System.out.println("(IOException) Unable to connect to " + cfg_data.irc_server_ip + ":" + cfg_data.irc_server_port);
			if (connectionToServer != null) {
				connectionToServer.terminate();
				connectionToServer = null;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Quits the bot.
	 */
	private void quit() {
		active = false;
		bot.disconnect();
		bot.quitServer();
		bot.shutdown();
	}
	
	//-------------------------------------------------------------------------
	//
	// IRC event handler methods
	//
	//-------------------------------------------------------------------------
	
	/**
	 * Have the bot handle message events
	 */
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		// Look for commands based on the specified token
		if (message.startsWith(cfg_data.irc_token)) {
			String[] keywords = message.split(" ");
			
			// There should be text for us to process, if not then don't bother
			if (keywords[0].length() > 1) {
				// Check the keywords for what to do
				switch (keywords[0].substring(1)) {
				case "autorestart":
					break;
				case "broadcast":
					break;
				case "commands":
					break;
				case "cpu":
					break;
				case "disconnect":
					break;
				case "file":
					break;
				case "get":
					break;
				case "help":
					break;
				case "host":
					break;
				case "kill":
					break;
				case "killall":
					break;
				case "killmine":
					break;
				case "killinactive":
					break;
				case "liststartwads":
					break;
				case "load":
					break;
				case "notice":
					break;
				case "off":
					break;
				case "on":
					break;
				case "owner":
					break;
				case "protect":
					break;
				case "query":
					break;
				case "quit":
					quit();
					break;
				case "rcon":
					break;
				case "save":
					break;
				case "send":
					messagesToSend.addLast("message here");
					break;
				case "servers":
					break;
				case "slot":
					break;
				case "uptime":
					break;
				case "whoami":
					break;
				default:
					break;
				}
			}
		}
	}
	
	/**
	 * Have the bot handle private message events
	 * @param sender The IRC data of the sender
	 * @param login The IRC data of the sender
	 * @param hostname The IRC data of the sender
	 * @param message The message transmitted
	 */
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		// Private messages here
	}

	/**
	 * Have the bot handle kicks, this is useful for rejoining when kicked.
	 * @param channel String - the channel
	 * @param kickerNick String - the name of the kicker
	 * @param kickerLogin String - the login of the kicker
	 * @param kickerHostname String - the hostname of the kicker
	 * @param recipientNick String - the name of the kicked user
	 * @param reason String - the reason for being kicked
	 */
	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
		//if (recipientNick.equalsIgnoreCase(getNick()) && channel.equalsIgnoreCase(cfg_data.irc_channel))
			//joinChannel(cfg_data.irc_channel);
	}
	
	/**
	 * This message is received from the connection, which most likely should
	 * be printed to the main channel.
	 */
	@Override
	public void processMessage(String message) {
		sendMessageToMainChannel(message);
	}

	/**
	 * This is made a thread because for testing there needs to be both an IRC
	 * bot and a server thread running to test communications. It's annoying to
	 * test it, so I just start a thread up and run it so the bot runs on it's 
	 * own, while the server can run on it's own thread.
	 */
	@Override
	public void run() {
		// Set up static and non-exception throwing fields
		irc_bot_starttime = System.currentTimeMillis();
		
		// Read config and connect to IRC (essential)
		try {
			// Read the config data
			cfg_data = new IRCConfigData(this.iniPath);
			
			// Set the log file
			Logger.setLogFile(cfg_data.global_logfile);

			// Set up the bot
			this.bot = new PircBotX();
			bot.getListenerManager().addListener(new BotIRCListener(this));
            bot.setName(cfg_data.irc_name);
            bot.setLogin(cfg_data.irc_username);
            bot.connect(cfg_data.irc_network, cfg_data.irc_port);
            bot.identify(cfg_data.irc_pass);
            bot.joinChannel(cfg_data.irc_channel);
		} catch (ConfigException cfge) {
			System.out.println("Error loading config data: " + cfge.getMessage());
			cfge.printStackTrace();
			quit();
			return;
		} catch (NickAlreadyInUseException e) {
			System.out.println("Error nick in use: " + e.getMessage());
			quit();
			return;
		} catch (IOException e) {
			System.out.println("Error IOError: " + e.getMessage());
			e.printStackTrace();
			quit();
			return;
		} catch (IrcException e) {
			System.out.println("Error IrcException: " + e.getMessage());
			e.printStackTrace();
			quit();
			return;
		}
		
		// While our bot is active, do required tasks
		active = true;
		while (active) {
			// If there is no connection, set one up
			if (active && (connectionToServer == null || connectionToServer.isClosed() || !connectionToServer.isConnected())) {
				if (!connectToServer()) {
					try {
						Thread.sleep(15000); // Attempt to reconnect in 15 seconds
					} catch (InterruptedException e) {
					}
				}
			// else if we have a connection, poll it
			} else {
				boolean connectionFine = true;
				// This will send the message to this object, if there is any
				// kind of error, the connection should be dropped
				if (connectionToServer.hasMessageReady()) {
					if (!connectionToServer.readMessage()) {
						sendMessageToMainChannel("Reading message failure, killing connection.");
						connectionToServer.terminate();
						connectionToServer = null;
						connectionFine = false;
					}
				}
				
				// If it's been a certain time, send a ping
				if (connectionFine && System.currentTimeMillis() - lastPingSent > Connection.PING_MAX_DURATION) {
					lastPingSent = System.currentTimeMillis();
					if (!connectionToServer.sendClientMessage("ping")) {
						sendMessageToMainChannel("Send ping message failure, killing connection.");
						connectionToServer.terminate();
						connectionToServer = null;
						connectionFine = false; // In case we use this later on
					}
				}
				
				// If we have messages to send, send them now
				// If our connection is messed up somehow then this should be aborted beforehand
				while (connectionFine && !messagesToSend.isEmpty()) {
					String messageToSend;
					messageToSend = messagesToSend.getFirst();
					// If sending it fails, then re-add it back into the queue,
					// and terminate the connection since something is wrong
					// Otherwise if it's delivered, then all is good
					if (!connectionToServer.sendClientMessage(messageToSend)) {
						sendMessageToMainChannel("Send message failure, killing connection.");
						messagesToSend.addFirst(messageToSend);
						connectionToServer.terminate();
						connectionToServer = null;
						connectionFine = false;
					} else {
						messagesToSend.removeFirst();
					}
				}
			}
			
			// Don't choke the OS
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
