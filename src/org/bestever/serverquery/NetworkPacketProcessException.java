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

public class NetworkPacketProcessException extends Exception {

	/**
	 * Generated UID
	 */
	private static final long serialVersionUID = 2329146796395286679L;

	/**
	 * A standard exception with no reason
	 */
	public NetworkPacketProcessException() {
		super();
	}

	/**
	 * An exception with a reason to indicate what happened
	 * @param msg A string of why it happened
	 */
	public NetworkPacketProcessException(String msg) {
		super(msg);
	}
}
