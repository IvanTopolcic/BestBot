package org.bestever.bebot;

/**
 * This contains an enumeration that is used for determining account status when 
 * people try to host with the bot
 */
public enum AccountType {
	GUEST (0, "guest"), 
	TRUSTED (1, "trusted"), 
	MODERATOR (2, "moderator"),
	ADMIN (3, "admin"); 
    
	/**
	 * This contains the index of the account (its a constant really)
	 */
	private final int account_type;
    
	/**
	 * The account name is stored here for easy reference/string output
	 */
	private final String account_type_name;
    
	/**
	 * This initializes the enumeration types
	 * @param type The index of the account
	 * @param name A string containing the name
	 */
	AccountType(int type, String name) {
		this.account_type = type;
		this.account_type_name = name;
	}
    
	/**
	 * Returns the constant index of the type
	 * @return An integer represeting a constant of the class
	 */
	int getAccountIndex() {
		return this.account_type;
	}
    
	/**
	 * Used to retrieve the account name associated with the enumeration
	 * @return A string containing a lower case name
	 */
	String getAccountName() {
		return this.account_type_name;
	}
    
	/**
	 * This will return the object if you have the enumeration index number
	 * @param index The number constant
	 * @return The object type that matches up with the index
	 */
	AccountType getAccountTypeFromNumber(int index) {
		switch (index) {
			case 3:
				return ADMIN;
			case 2:
				return MODERATOR;
			case 1:
				return TRUSTED;
			case 0: // Guests should act like the default, fall through
			default:
				return GUEST;
		}
	}
    
	/**
	 * This will return the object if you have the enumeration index number
	 * @param name The name to check
	 * @return The object type that matches up with the index
	 */
	AccountType getAccountTypeFromName(String name) {
		switch (name.toLowerCase()) {
			case "admin":
			case "administrator":
				return ADMIN;
			case "mod":
			case "moderator":
				return MODERATOR;
			case "trusted":
				return TRUSTED;
			case "guest": // Guests should act like the default, fall through
			default:
				return GUEST;
		}
	}
}