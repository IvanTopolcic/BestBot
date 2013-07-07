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

package org.bestever.external;

/**
 * A list of all the flags we want to request in a query from the server
 */
public class ServerQueryFlags {
	
	/**
	 * This is 199 in little endian
	 */
	public static final int LAUNCHER_CHALLENGE = 0xC7000000;
	
	// These are what we want
	public static final int SQF_PWADS = 0x00000040;
	public static final int SQF_IWAD = 0x00000200;
	public static final int SQF_GAMESKILL = 0x00001000;
	public static final int SQF_ALL_DMFLAGS	= 0x08000000;
	
	// This is what we will send to the server
	public static final int SQF_ALL_REQUEST_FLAGS = SQF_PWADS | SQF_IWAD | SQF_GAMESKILL | SQF_ALL_DMFLAGS;
	
	// Game mode enumeration
	public static final int GAMEMODE_COOPERATIVE = 0;
	public static final int GAMEMODE_SURVIVAL = 1;
	public static final int GAMEMODE_INVASION = 2;
	public static final int GAMEMODE_DEATHMATCH = 3;
	public static final int GAMEMODE_TEAMPLAY = 4;
	public static final int GAMEMODE_DUEL = 5;
	public static final int GAMEMODE_TERMINATOR = 6;
	public static final int GAMEMODE_LASTMANSTANDING = 7;
	public static final int GAMEMODE_TEAMLMS = 8;
	public static final int GAMEMODE_POSSESSION = 9;
	public static final int GAMEMODE_TEAMPOSSESSION = 10;
	public static final int GAMEMODE_TEAMGAME = 11;
	public static final int GAMEMODE_CTF = 12;
	public static final int GAMEMODE_ONEFLAGCTF = 13;
	public static final int GAMEMODE_SKULLTAG = 14;
	public static final int GAMEMODE_DOMINATION = 15;
}
