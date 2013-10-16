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

package org.bestever.serverquery;

/**
 * A list of all the flags we want to request in a query from the server
 */
public final class ServerQueryFlags {
	
	/**
	 * This is 199 in little endian
	 */
	public static final int LAUNCHER_CHALLENGE = 0xC7000000;
	
	// These are what we want
	public static final int SQF_NAME = 0x00000001;
	public static final int SQF_URL = 0x00000002;
	public static final int SQF_EMAIL = 0x00000004;
	public static final int SQF_MAPNAME = 0x00000008;
	public static final int SQF_MAXCLIENTS = 0x00000010;
	public static final int SQF_MAXPLAYERS = 0x00000020;
	public static final int SQF_PWADS = 0x00000040;
	public static final int SQF_GAMETYPE = 0x00000080;
	public static final int SQF_GAMENAME = 0x00000100;
	public static final int SQF_IWAD = 0x00000200;
	public static final int SQF_FORCEPASSWORD = 0x00000400;
	public static final int SQF_FORCEJOINPASSWORD = 0x00000800;
	public static final int SQF_GAMESKILL = 0x00001000;
	public static final int SQF_BOTSKILL = 0x00002000;
	public static final int SQF_DMFLAGS = 0x00004000;
	public static final int SQF_LIMITS = 0x00010000;
	public static final int SQF_TEAMDAMAGE = 0x00020000;
	public static final int SQF_TEAMSCORES = 0x00040000;
	public static final int SQF_NUMPLAYERS = 0x00080000;
	public static final int SQF_PLAYERDATA = 0x00100000;
	public static final int SQF_TEAMINFO_NUMBER = 0x00200000;
	public static final int SQF_TEAMINFO_NAME = 0x00400000;
	public static final int SQF_TEAMINFO_COLOR = 0x00800000;
	public static final int SQF_TEAMINFO_SCORE = 0x01000000;
	public static final int SQF_TESTING_SERVER = 0x02000000;
	public static final int SQF_DATA_MD5SUM = 0x04000000;
	public static final int SQF_ALL_DMFLAGS = 0x08000000;
	public static final int SQF_SECURITY_SETTINGS = 0x10000000;
	
	// This is what we will send to the server
	public static final int SQF_ALL_REQUEST_FLAGS = SQF_PWADS | SQF_IWAD | SQF_GAMETYPE | SQF_GAMESKILL | SQF_ALL_DMFLAGS;
	
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
	
	// Server query response flags
	public static final int SQRF_BEGINNING = 0x565d77; //5660023
	public static final int SQRF_TOOQUICKREQUEST = 0x565d78; //5660024
	public static final int SQRF_IPISBANNED = 0x565d79; //5660025
	
	/**
	 * Gets the gamemode flag from the received byte
	 * @param flag The constant number from the SQF result
	 * @return A string that the bot will recognize to host with
	 */
	public static String getGamemodeFromFlag(int flag) {
		switch (flag) {
		case GAMEMODE_COOPERATIVE:
			return "cooperative";
		case GAMEMODE_SURVIVAL:
			return "survival";
		case GAMEMODE_INVASION:
			return "invasion";
		case GAMEMODE_DEATHMATCH:
			return "deathmatch";
		case GAMEMODE_TEAMPLAY:
			return "teamplay"; // TDM
		case GAMEMODE_DUEL:
			return "duel";
		case GAMEMODE_TERMINATOR:
			return "terminator";
		case GAMEMODE_LASTMANSTANDING:
			return "lastmanstanding";
		case GAMEMODE_TEAMLMS:
			return "teamlms";
		case GAMEMODE_POSSESSION:
			return "possession";
		case GAMEMODE_TEAMPOSSESSION:
			return "teampossession";
		case GAMEMODE_TEAMGAME:
			return "teamgame";
		case GAMEMODE_CTF:
			return "ctf";
		case GAMEMODE_ONEFLAGCTF:
			return "oneflagctf";
		case GAMEMODE_SKULLTAG:
			return "skulltag";
		case GAMEMODE_DOMINATION:
			return "domination";
		}
		return "ERROR";
	}
}
