package org.bestever.bebot;

public class AccountType {
	
	/**
	 * These are generic unknown accounts that have just registered
	 */
	public static final int GUEST = 0; // 0
	
	/**
	 * Admin users have access to everything
	 */
	public static final int ADMIN = 1 << 0; // 1
	
	/**
	 * Moderators are allowed to do various additional functions most users cant
	 */
	public static final int MODERATOR = 1 << 1; // 2
	
	/**
	 * Trusted users may have access to things most users don't have access to (ex: IRC bots...etc)
	 */
	public static final int TRUSTED = 1 << 2; // 4
	
}