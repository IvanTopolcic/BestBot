package org.bestever.bebot;

import java.util.ListIterator;

/**
 * This is a garbage test class to find the best way to manage server/player active time.
 * Please do not use this yet!
 * @author Ivan
 *
 */
@SuppressWarnings("unused")
public class Uptime implements Runnable {
	
	Bot bot;

	@Override
	public void run() {
		calculateUptime();
	}
	
	public Uptime(Bot bot) {
		this.bot = bot;
	}
	
	public void calculateUptime() {
		while (true) {
			ListIterator<Server> it = bot.servers.listIterator();
			Server s = null;
			while (it.hasNext()) {
				s = it.next();
			}
		}
	}
}
