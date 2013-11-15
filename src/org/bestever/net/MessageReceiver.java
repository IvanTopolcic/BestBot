package org.bestever.net;

/**
 * Indicates a class that can receive messages from a MessageSender. This is
 * useful for sockets where the inbound messages need to be processed by
 * another 
 */
public interface MessageReceiver {
	
	void processMessage(String message);
}
