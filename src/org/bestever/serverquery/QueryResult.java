package org.bestever.serverquery;

/**
 * A loose encapsulation of the query result
 */
public class QueryResult {
	
	/**
	 * A list of all the pwads
	 */
	public String pwad_names;

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
		this.pwad_names = null;
		this.gamemode = -1;
		this.instagib = -1;
		this.buckshot = -1;
		this.iwad = null;
		this.skill = -1;
		this.dmflags = -1;
		this.dmflags2 = -1;
		this.dmflags3 = -1;
		this.compatflags = -1;
		this.compatflags2 = -1;
	}
}
