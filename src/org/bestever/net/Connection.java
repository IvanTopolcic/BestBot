package org.bestever.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * A non-threaded TCP connection that should be polled for information.
 */
public class Connection {

	/**
	 * The socket which connects us to the recipient.
	 */
	private Socket socket;
	
	/**
	 * The reader that reads the socket for data.
	 */
	private BufferedReader reader;
	
	/**
	 * The writer for the socket.
	 */
	private DataOutputStream writer;
	
	/**
	 * What object will receive our incoming message.
	 */
	private MessageReceiver messageReceiver;
	
	/**
	 * The last time in milliseconds we got a message.
	 */
	private long lastMessageReceivedTime;
	
	/**
	 * The last time we sent a ping
	 */
	private long lastMessageSentTime;
	
	/**
	 * Send a ping every 15 seconds.
	 */
	public static final long PING_MAX_DURATION = 15000L;
	
	/**
	 * This is how long in milliseconds before we consider a connection to have
	 * timed out (and should then be killed).
	 */
	public static final long CONNECTION_TIMEOUT_MS = 30000L;
	
	/**
	 * Creates a new connection from an already activated socket. Sets
	 * TcpNoDelay to true.
	 * @param socket The active socket.
	 * @param messageReceiver The object to receive messages from this
	 * connection.
	 * @throws SocketException The exception thrown if the socket passed has 
	 * problems.
	 * @throw IOException If there was a problem with setting up the reader or
	 * writer.
	 */
	public Connection(Socket socket, MessageReceiver messageReceiver) throws SocketException, IOException {
		if (socket == null || !socket.isConnected() || socket.isClosed())
			throw new SocketException("Socket is either null/closed/not connected.");
		this.socket = socket;
		this.socket.setTcpNoDelay(true);
		this.messageReceiver = messageReceiver;
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new DataOutputStream(socket.getOutputStream());
		this.lastMessageReceivedTime = System.currentTimeMillis();
	}
	
	/**
	 * Checks if the socket is connected.
	 * @return True if connected, false otherwise.
	 */
	public boolean isConnected() {
		return socket.isConnected();
	}
	
	/**
	 * Checks if the socket is closed.
	 * @return True if close, false otherwise.
	 */
	public boolean isClosed() {
		return socket.isClosed();
	}
	
	/**
	 * Gets the last time the System.currentTimeMillis was called when data was
	 * received by this connection.
	 * @return The time received (in milliseconds). This is <u>not</u> the time 
	 * elapsed which would require doing: <br>
	 * System.currentTimeMillis() - getLastTimeMessageReceived();
	 */
	public long getLastTimeMessageReceived() {
		return lastMessageReceivedTime;
	}
	
	/**
	 * Gets the last time the System.currentTimeMillis was called when data was
	 * set to be sent by this connection.
	 * @return The time received (in milliseconds). This is <u>not</u> the time 
	 * elapsed which would require doing: <br>
	 * System.currentTimeMillis() - getLastTimeMessageSent();
	 */
	public long getLastTimeMessageSent() {
		return lastMessageSentTime;
	}
	
	/**
	 * Checks if a message can be read from the stream. If there is any
	 * exception thrown from checking if there is an available set of data,
	 * this will return false.
	 * @return True if data is ready, false if not or there's an IO error.
	 */
	public boolean hasMessageReady() {
		try {
			return reader.ready();
		} catch (IOException e) {
			System.out.println("IOException reading reader.ready() : " + e.getMessage());
		}
		return false;
	}
	
	/**
	 * Reads a message and sends it to the messageReceiver object. The program
	 * should call hasMessageReady() before running this. It checks to make
	 * sure there is a message ready, but for safety it's best to run the check
	 * beforehand.
	 * @return True if a message was read, false if there is no message ready
	 * or there is an error.
	 */
	public boolean readMessage() {
		String line = "";
		try {
			if (reader.ready()) {
				if ((line = reader.readLine()) != null) {
					messageReceiver.processMessage(line);
					lastMessageReceivedTime = System.currentTimeMillis();
					return true;
				} else
					System.out.println("reader.readLine() is null...");
			}
		} catch (IOException e) {
			System.out.println("Error reading socket [readMessage()]: " + e.getMessage());
		}
		return false;
	}
	
	/**
	 * When invoked, writes the message out through the connection.
	 * @param msg The message to send.
	 * @return True if the message was sent without an exception, false if it
	 * was not sent and there was an error.
	 */
	public boolean sendClientMessage(String msg) {
		String message = msg;
		// Readline from the other side will need the new line, add it if it doesn't exist
		if (message.charAt(message.length() - 1) != '\n')
			message = new String(msg + '\n');
		try {
			writer.writeBytes(message);
			writer.flush();
			lastMessageSentTime = System.currentTimeMillis();
			return true;
		} catch (IOException e) {
			System.out.println("Error writing data (" + msg + "): " + e.getMessage());
		}
		return false;
	}
	
	/**
	 * Kills this connection, thread, and the subthreads for read/write.
	 */
	public void terminate() {
		// Close the reader
		try {
			reader.close();
		} catch (IOException e) {
			System.out.println("Error closing the reader for connection " + this + ": " + e.getMessage());
		}
		
		// Close the writer
		try {
			writer.close();
		} catch (IOException e1) {
			System.out.println("Error closing the writer for connection " + this + ": " + e1.getMessage());
		}
		
		// Close the socket
		try {
			socket.close();
		} catch (IOException e2) {
			System.out.println("Error closing the socket in Connection.terminate() : " + e2.getMessage());
		}
	}
}
