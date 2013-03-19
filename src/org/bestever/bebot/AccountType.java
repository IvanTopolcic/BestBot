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
	
	/**
	 * To check for different masks, this method searches to see if you contain one of them.
	 * Usage of this function would be similar to: isAccountType(accountHere, AccountType.ADMIN, AccounType.TRUSTED);
	 * to check if they are either an admin or trusted user
	 * @param accountType The bitmask to check of the account
	 * @param types A list of constants (see AccountType enumerations)
	 * @return True if one of the types is met, false if none are
	 */
	public static boolean isAccountTypeOf(int accountType, int... types) {
		for (int i = 0; i < types.length; i++) {
			// If we ask for the GUEST mask, then it will always return true as that's the default one
			if (types[i] == GUEST)
				return true;
			
			// Check and see if the accountType matches our desired type
			if ((accountType & types[i]) == types[i])
				return true;
		}
		
		// If we didn't find any matches, then they don't match
		return false;
	}
}