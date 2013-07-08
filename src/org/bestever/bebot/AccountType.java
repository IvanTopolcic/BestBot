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

public class AccountType {
	
	/**
	 * Below are the bitmask permissions for userroups
	 **/
	public static final int GUEST = 0; // 0
	public static final int REGISTERED = 1 << 0; // 1
	public static final int MODERATOR = 1 << 1; // 2
	public static final int ADMIN = 1 << 2; // 4
	public static final int RCON = 1 << 3; // 8
	
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
			if ((accountType & types[i]) == types[i])
				return true;
		}
		// If we didn't find any matches at all
		return false;
	}
}