package org.bestever.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ConnectionManager implements Runnable {

	/**
	 * The port to listen on.
	 */
	private int listenPort;
	
	/**
	 * Where to send our TCP connections so they can create servers through the
	 * server manager.
	 */
	private ServerManager serverManager;
	
	/**
	 * Listens for incoming connections.
	 */
	private ServerSocket socketListener;
	
	/**
	 * Determines whether the thread will continue running (true) or not
	 * (false).
	 */
	private boolean active;
	
	/**
	 * How long our pulse of listening goes for until the program decides to
	 * check again if it's closed or not.
	 */
	public static final int SOCKET_TIMEOUT_MS = 5000;
	
	/**
	 * Creates a basic connection manager listener. This is not active, and
	 * should be linked with a ServerManager using the .linkToServerManager()
	 * method. Once this is done, then .start() can be run on this when put
	 * into a thread.
	 * @param listenPort The port to listen on.
	 * @throws IndexOutOfBoundsException If the port is invalid.
	 */
	public ConnectionManager(int listenPort) {
		if (listenPort < 1 || listenPort > 63354)
			throw new IndexOutOfBoundsException("Port number is not in a valid range (must be between 1 - 65534 inclusive).");
		this.active = false;
		this.listenPort = listenPort;
		this.serverManager = null;
		this.socketListener = null;
	}
	
	/**
	 * Provides a reference to the ServerManager object that will be passed
	 * open connections.
	 * @param serverManager The ServerManager object to recieve input.
	 */
	public void linkToServerManager(ServerManager serverManager) {
		this.serverManager = serverManager;
	}
	
	/**
	 * Checks to see if the thread has been activated or not. False means it
	 * is either not started, or has been terminated.
	 * @return True if thread is running and listening for connections, false
	 * otherwise.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Shuts down everything related to this object and causes the thread to
	 * finish as soon as it can.
	 */
	public void terminate() {
		active = false;
		serverManager = null;
	}
	
	/**
	 * Sets up the socket listener for listening.
	 * @return True if it was set up properly, false otherwise.
	 */
	private boolean setupSocketListener() {
		try {
			socketListener = new ServerSocket(listenPort);
			socketListener.setSoTimeout(SOCKET_TIMEOUT_MS);
		} catch (IOException e) {
			System.out.println("Error trying to listen on port " + listenPort + ".");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void run() {
		// Set up the socket listener, if it returns false something is wrong
		// and thus the thread should not bother continuing
		if (!setupSocketListener())
			return;
		
		// With the port now listening, do the listening
		active = true;
		Socket socket = null;
		while (active) {
			try {
				// Block until we get a connection
				socket = socketListener.accept();
				System.out.println("Got connection from: " + socket.getInetAddress() + ":" + socket.getPort());
				
				// If we get a connection and are ready to process it...
				if (serverManager != null && active) {
					serverManager.acceptConnection(socket);
				} else {
					System.out.println("Server manager is null or we're not active, terminating recent connection.");
					try {
						socket.close(); // If were shutting down or can't handle the connection, close it
					} catch (IOException ioe) {
						System.out.println("Error closing socket we are refusing to accept.");
						ioe.printStackTrace();
					} finally {
						socket = null;
					}
				}
			} catch (SocketTimeoutException ste) {
				// Do nothing, timeouts are expected
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Always attempt to safely shut down the socket listener
		try {
			socketListener.close();
		} catch (IOException e) {
			System.out.println("Error shutting down socketListener at the end.");
			e.printStackTrace();
		}
	}	
}
