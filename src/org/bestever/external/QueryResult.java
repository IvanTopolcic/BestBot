package org.bestever.external;

/**
 * A loose encapsulation of the query result
 */
public class QueryResult {
	
	/**
	 * How many pwads there are
	 */
	private byte num_of_pwads;
	
	/**
	 * A list of all the pwads
	 */
	public String[] pwad_names;

	/**
	 * The gamemode constant (can be found in ServerQueryFlags
	 */
	public byte gamemode;
	
	/**
	 * If instagib is on (acts like a boolean)
	 */
	public byte instagib;
	
	/**
	 * If buckshot is on (acts like a boolean)
	 */
	public byte buckshot;

	/**
	 * What iwad is being used
	 */
	public String iwad;

	/**
	 * The skill level of the server
	 */
	public byte skill;

	/**
	 * In-game flags bitmask
	 */
	public int dmflags;
	
	/**
	 * In-game flags bitmask
	 */
	public int dmflags2;
	
	/**
	 * In-game flags bitmask
	 */
	public int dmflags3;
	
	/**
	 * In-game flags bitmask
	 */
	public int compatflags;
	
	/**
	 * In-game flags bitmask
	 */
	public int compatflags2;
	
	/**
	 * Default constructor for now
	 */
	public QueryResult() {
	}
	
	/**
	 * This should be used instead of
	 * @param amount How many pwads there are to set
	 */
	public void setNumOfPwads(byte amount) {
		if (amount < 0)
			throw new NetworkBufferException("setNumOfPwads got a negative number from the NetworkBuffer. Contact an administrator.");
		pwad_names = new String[amount];
	}
	
	/**
	 * Returns how many pwads there are loaded onto the server
	 * @return The number of pwads in byte form
	 */
	public byte getNumOfPwads() {
		return num_of_pwads;
	}
}
