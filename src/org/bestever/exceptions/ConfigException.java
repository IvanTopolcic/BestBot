package org.bestever.exceptions;

public class ConfigException extends Exception {

	/**
	 * Generated serial UID.
	 */
	private static final long serialVersionUID = -3250241430021050518L;

	/**
	 * Creates a basic config exception.
	 */
	public ConfigException() {
		super();
	}
	
	/**
	 * Creates a config exception with a reason.
	 * @param reason The reason for the error.
	 */
	public ConfigException(String reason) {
		super(reason);
	}
}
