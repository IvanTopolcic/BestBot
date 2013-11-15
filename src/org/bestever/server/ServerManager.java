package org.bestever.server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;

import org.bestever.net.Connection;
import org.bestever.net.MessageReceiver;

public class ServerManager implements Runnable, MessageReceiver {

	/**
	 * A list of all connections from the outside.
	 */
	private LinkedList<Connection> connections;
	
	/**
	 * If the server manager is still running.
	 */
	private boolean active;
	
	/**
	 * Constructs a new ServerManager.
	 */
	public ServerManager() {
		connections = new LinkedList<>();
		active = false;
	}
	
	/**
	 * This will be passed an open socket that has not been verified. The
	 * socket will not be processed if its null, not bound, closed, or not
	 * connected.
	 * @param socket The socket to listen to.
	 */
	public void acceptConnection(Socket socket) {
		// If the socket is messed up, we will not accept it
		if (socket == null || !socket.isBound() || !socket.isConnected() || socket.isClosed())
			return;
		
		// Initialize the object
		Connection connection = null;
		try {
			connection = new Connection(socket, this);
		} catch (SocketException e) {
			System.out.println("Connection setup reader/writer failure: " + e.getMessage());
			return;
		} catch (IOException ioe) {
			System.out.println("Connection setup reader/writer failure: " + ioe.getMessage());
			return;
		}
		
		// Index it so we can poll and/or kill it later on safely
		connections.add(connection);
	}
	
	/**
	 * Closes down all the resources associated with this object. It will be
	 * unable to accept any more connections and will send out one final
	 * message to all connections that it is shutting down.
	 */
	public void terminate() {
		for (Connection c : connections)
			c.terminate();
		connections.clear();
		connections = null;
		active = false;
	}
	
	@Override
	public void processMessage(String message) {
		System.out.println("Got message: " + message);
	}

	@Override
	public void run() {
		active = true;
		while (active) {
			// Go through each connection and handle them all
			for (Connection c : connections) {
				// If it's been too long since we got a ping, kill it
				if (System.currentTimeMillis() - c.getLastTimeMessageReceived() > Connection.CONNECTION_TIMEOUT_MS) {
					System.out.println("Connection timed out, removing connection.");
					connections.remove(c);
					c.terminate();
					continue;
				}
				
				// Read incoming data
				if (c.hasMessageReady()) {
					if (!c.readMessage()) {
						System.out.println("Error reading message, destroying connection.");
						connections.remove(c);
						c.terminate();
						continue;
					}
				}
				
				// If it's been too long since our last ping, send it
				if (System.currentTimeMillis() - c.getLastTimeMessageSent() > Connection.PING_MAX_DURATION) {
					if (!c.sendClientMessage("ping")) {
						System.out.println("Error writing message, destroying connection.");
						connections.remove(c);
						c.terminate();
						continue;
					}
				}
			}
			
			// Don't choke the OS
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
