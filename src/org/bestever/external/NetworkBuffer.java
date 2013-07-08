package org.bestever.external;

import java.util.concurrent.LinkedBlockingQueue;

public class NetworkBuffer {
	
	/**
	 * How large this buffer is, any writing to it when it's full will probably
	 * throw some kind of error
	 */
	private int max_capacity;
	
	/**
	 * Indicates how many bytes are in the buffer and where it will start any
	 * byte additions to the data structure
	 */
	private int offset_pointer;
	
	/**
	 * The list of data for FIFO structure
	 */
	private LinkedBlockingQueue<Byte> data;
	
	/**
	 * Constructs a network buffer with 1024 bytes
	 */
	public NetworkBuffer() {
		this(1024);
	}
	
	/**
	 * Constructs a network buffer with a specified size
	 * @param size How many bytes to hold
	 */
	public NetworkBuffer(int size) {
		this.max_capacity = size;
		this.offset_pointer = 0;
		this.data = new LinkedBlockingQueue<>(size);
	}
	
	/**
	 * Returns the capacity of the buffer
	 * @return The number of bytes allowed in the buffer
	 */
	public int getCapacity() {
		return max_capacity;
	}
	
	/**
	 * Returns the pointer to where the buffer is at, which is equal to
	 * how many bytes are in the buffer
	 * @return The number of bytes in the buffer
	 */
	public int getNumberOfBytesInBuffer() {
		return offset_pointer;
	}
	
	/**
	 * Adds a byte to the buffer
	 * @param b The byte to add
	 */
	public void add(byte b) {
		data.add(b);
		offset_pointer++;
	}
	
	/**
	 * Adds the entire byte to the buffer
	 * @param b The byte array to add
	 */
	public void add(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			data.add(b[i]);
			offset_pointer++;
		}
	}
	
	/**
	 * Adds the byte from the beginning to length to the buffer
	 * @param b The data
	 * @param length The length to add [0 - length)
	 */
	public void add(byte[] b, int length) {
		for (int i = 0; i < length; i++) {
			data.add(b[i]);
			offset_pointer++;
		}
	}
	
	/**
	 * Extracts the data type from the front of the queue
	 * @return The byte from the front of the buffer
	 */
	public byte extractByte() {
		return 0;
	}
	
	/**
	 * Extracts the data type from the front of the queue
	 * @param littleEndian If true, will read the primitive as if it was little endian,
	 * otherwise it will read it as big endian 
	 * @return The short from the front of the buffer
	 */
	public short extractShort(boolean littleEndian) {
		return 0;
	}
	
	/**
	 * Extracts the data type from the front of the queue
	 * @param littleEndian If true, will read the primitive as if it was little endian,
	 * otherwise it will read it as big endian 
	 * @return The int from the front of the buffer
	 */
	public int extractInt(boolean littleEndian) {
		return 0;
	}
	
	/**
	 * Extracts the data type from the front of the queue
	 * @param littleEndian If true, will read the primitive as if it was little endian,
	 * otherwise it will read it as big endian 
	 * @return The long from the front of the buffer
	 */
	public long extractLong(boolean littleEndian) {
		return 0;
	}
	
	/**
	 * Extracts the data type from the front of the queue, including the null terminated zero;
	 * there must be null termination in the string or else it will not know when to stop
	 * @return The string from the front of the buffer
	 */
	public String extractString() {
		return null;
	}
}
