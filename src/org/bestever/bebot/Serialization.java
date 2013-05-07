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

public class Serialization {
	
	/**
	 * Grabs the data from the mysql database and runs servers by passing their
	 * information off to a method in the bot that will process accordingly.
	 * This should be run at startup and only startup.
	 * @param bot The bot object that will have server data sent to it.
	 * @param mysql The MySQL object which contains the database information.
	 */
	public static void pullServerData(Bot bot, MySQL mysql) {
		// Unimplemented yet
	}
}