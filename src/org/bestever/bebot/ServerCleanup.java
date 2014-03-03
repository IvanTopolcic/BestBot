// --------------------------------------------------------------------------
// Copyright (C) 2012-2013 Best-Ever
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// --------------------------------------------------------------------------

package org.bestever.bebot;

import java.util.TimerTask;

/**
 * This class runs every x amount of time and is responsible for
 * killing any inactive servers as well as any servers running
 * for longer than 30 days
 */
public class ServerCleanup extends TimerTask {

	/**
	 * Holds the bot
	 */
	private Bot bot;

	/**
	 * Constructor
	 * @param bot Bot - the main bot object
	 */
	public ServerCleanup(Bot bot) {
		this.bot = bot;
	}

	/**
	 * Send a message to all servers
	 */
	public void run() {
		int killed = 0;
		for (Server s : bot.servers) {
			// Check if the server has been running for more than 3 days without activity
			if (System.currentTimeMillis() - s.serverprocess.last_activity > Server.DAY_MILLISECONDS * bot.cfg_data.cleanup_interval) {
				s.hide_stop_message = true;
				s.killServer();
				killed++;
			}
		}
		// Send a message to the channel if we've killed a server like this
		if (killed > 0) {
			bot.sendMessage(bot.cfg_data.irc_channel, Functions.pluralize("Killed " + killed + " inactive server{s} (inactive for " + bot.cfg_data.cleanup_interval + " days.", killed));
		}
	}
}
