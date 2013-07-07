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
