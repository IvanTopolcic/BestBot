package org.bestever.bebot;

import java.util.TimerTask;

/**
 * Class that runs timed broadcasts (notices, etc)
 */
public class NoticeTimer extends TimerTask {

	/**
	 * Holds the bot
	 */
	private Bot bot;

	/**
	 * Constructor
	 * @param bot Bot - the main bot object
	 */
	public NoticeTimer(Bot bot) {
		this.bot = bot;
	}

	/**
	 * Send a message to all servers
	 */
	public void run() {
		for (Server s : bot.servers)
			s.in.println("say " + bot.cfg_data.bot_notice);
	}
}
