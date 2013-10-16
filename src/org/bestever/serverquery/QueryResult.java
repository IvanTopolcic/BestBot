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
