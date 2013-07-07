package org.bestever.external;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class QueryHandler extends Thread {

	/**
	 * The object to notify when we are done
	 */
	private QueryManager handlerThread;
	
	/**
	 * The requested IP/port to query
	 */
	private ServerQueryRequest request;
	
	/**
	 * If we don't hear from the server in 10 seconds, then consider it dead/not working
	 */
	public static final int SOCKET_TIMEOUT_MS = 10000;
	
	/**
	 * This constructs the object so upon .run() it will gather the data and send it to the bot properly <br>
	 * Note that this does NOT invoke .run() automatically, the user must do so or else there
	 * will never be proper communication to the query manager about being done
	 * @param request The requested site
	 * @param handlerThread The query manager that we shall signal when we are done
	 */
	public QueryHandler(ServerQueryRequest request, QueryManager handlerThread) {
		this.request = request;
		this.handlerThread = handlerThread;
	}
	
	public void run() {
		// If any of these stay the same then something is wrong
		InetAddress IPAddress = null;
		int port = 0;
		byte[] dataToSend;
		byte[] dataToReceive = new byte[2048]; // Doubled standard size in case there's some dumb wad list with a lot of characters
		
		// Try with resources, we want to always have the socket close
		try (DatagramSocket connectionSocket = new DatagramSocket()) {
			// Setup our connection data
			IPAddress = InetAddress.getByName(request.getIP());
			port = request.getPort();
			
			// Put our data into a buffer and transform it before sending
			ByteBuffer byteBuffer = ByteBuffer.allocate(4);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			byteBuffer.putInt(ServerQueryFlags.SQF_ALL_REQUEST_FLAGS);
			byteBuffer.rewind();
			dataToSend = Huffman.encode(byteBuffer.array());
			
			// Now send the data
			DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, IPAddress, port);
			connectionSocket.send(sendPacket);
			
			// Block until we receive something or time out
			DatagramPacket receivePacket = new DatagramPacket(dataToReceive, dataToReceive.length);
			connectionSocket.setSoTimeout(SOCKET_TIMEOUT_MS);
			connectionSocket.receive(receivePacket);
			System.out.println("Got request back!");
			//byte string; byte byte byte; string; byte byte int int int int int
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Always alert the handler thread we are done
		handlerThread.signalProcessQueryComplete();
	}
}
