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

package org.bestever.external;

/**
 * Fusion between a tuple and InetSocketAddress
 */
public class ServerQueryRequest {

	/**
	 * The ip of the server to parse, does not accept hostnames right now
	 */
	private String ip;
	
	/**
	 * The port the server is on
	 */
	private int port;
	
	/**
	 * Creates a new request which can be passed to a queue; all the data values
	 * should be checked before hand, this does no fail-safe checking
	 * @param ip The IP of the server
	 * @param port The port the server is on
	 */
	public ServerQueryRequest(String ip, int port) {
		this.ip = new String(ip);
		this.port = port;
	}
	
	/**
	 * Returns a copy of the IP
	 * @return A string of the IP
	 */
	public String getIP() {
		return new String(ip);
	}
	
	/**
	 * Gets the desired port
	 * @return Port as an integer
	 */
	public int getPort() {
		return port;
	}
}
