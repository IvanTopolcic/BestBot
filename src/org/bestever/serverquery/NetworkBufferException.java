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
 * Indicates something has gone wrong in the network buffer
 */
public class NetworkBufferException extends RuntimeException {

	/**
	 * Serial ID of this object
	 */
	private static final long serialVersionUID = 2556273329727331623L;

	/**
	 * A standard exception with no reason
	 */
	public NetworkBufferException() {
		super();
	}

	/**
	 * An exception with a reason to indicate what happened
	 * @param msg A string of why it happened
	 */
	public NetworkBufferException(String msg) {
		super(msg);
	}
}
