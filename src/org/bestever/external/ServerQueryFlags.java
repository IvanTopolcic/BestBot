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
}
