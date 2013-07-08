package org.bestever.external;

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
