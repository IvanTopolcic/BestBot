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

package org.bestever.serverquery;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Encapsulation of inbound network data, can be extended to outbound as well
 */
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
		if (offset_pointer + 1 > max_capacity)
			throw new NetworkBufferException("Buffer is full, adding a byte would cause an overflow.");
		data.add(b);
		offset_pointer++;
	}
	
	/**
	 * Adds the entire byte to the buffer
	 * @param b The byte array to add
	 */
	public void add(byte[] b) {
		if (offset_pointer + b.length > max_capacity)
			throw new NetworkBufferException("Buffer is full, adding a byte array would cause an overflow.");
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
		if (offset_pointer + length > max_capacity)
			throw new NetworkBufferException("Buffer is full, adding a byte array with length would cause an overflow.");
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
		if (offset_pointer - 1 < 0)
			throw new NetworkBufferException("Buffer is empty, cannot extract a byte.");
		offset_pointer--;
		return data.poll();
	}
	
	/**
	 * Extracts the data type from the front of the queue
	 * @param littleEndian If true, will read the primitive as if it was little endian,
	 * otherwise it will read it as big endian 
	 * @return The short from the front of the buffer
	 */
	public short extractShort(boolean littleEndian) {
		if (offset_pointer - 2 < 0)
			throw new NetworkBufferException("Buffer is empty, cannot extract a short.");
		ByteBuffer byteBuffer = ByteBuffer.allocate(2);
		byteBuffer.order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		for (int i = 0; i < 2; i++)
			byteBuffer.put(data.poll()); // Unroll me one day
		offset_pointer -= 2;
		byteBuffer.rewind();
		return byteBuffer.getShort();
	}
	
	/**
	 * Extracts the data type from the front of the queue
	 * @param littleEndian If true, will read the primitive as if it was little endian,
	 * otherwise it will read it as big endian 
	 * @return The int from the front of the buffer
	 */
	public int extractInt(boolean littleEndian) {
		if (offset_pointer - 4 < 0)
			throw new NetworkBufferException("Buffer is empty, cannot extract an integer.");
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		byteBuffer.order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		for (int i = 0; i < 4; i++)
			byteBuffer.put(data.poll()); // Unroll me one day
		offset_pointer -= 4;
		byteBuffer.rewind();
		return byteBuffer.getInt();
	}
	
	/**
	 * Extracts the data type from the front of the queue
	 * @param littleEndian If true, will read the primitive as if it was little endian,
	 * otherwise it will read it as big endian 
	 * @return The long from the front of the buffer
	 */
	public long extractLong(boolean littleEndian) {
		if (offset_pointer - 8 < 0)
			throw new NetworkBufferException("Buffer is empty, cannot extract a long.");
		ByteBuffer byteBuffer = ByteBuffer.allocate(8);
		byteBuffer.order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		for (int i = 0; i < 8; i++)
			byteBuffer.put(data.poll()); // Unroll me one day
		offset_pointer -= 8;
		byteBuffer.rewind();
		return byteBuffer.getLong();
	}
	
	/**
	 * Extracts the data type from the front of the queue, including the null terminated zero;
	 * there must be null termination in the string or else it will not know when to stop
	 * @return The string from the front of the buffer
	 */
	public String extractString() {
		if (offset_pointer <= 0)
			throw new NetworkBufferException("Buffer is empty, cannot extract a string.");
		StringBuilder sb = new StringBuilder(max_capacity); // Just to be safe in case the string is the length of our array
		Byte b = null;
		for (int i = 0; i < offset_pointer; i++) {
			b = data.poll();
			if (b == null)
				throw new NetworkBufferException("Buffer offset pointer was desynchronized from the data, attempted extraction of a non-existing byte.");
			offset_pointer--;
			if (b == 0)
				break; // End of string while still taking the null terminator out
			sb.append((char)((byte)b));
		}
		String returnString = sb.toString();
		if (returnString == null)
			throw new NetworkBufferException("String extraction resulted in a null string.");
		else if (returnString.equals(""))
			throw new NetworkBufferException("String extraction resulted in an empty string.");
		return returnString;
	}
	
	/**
	 * Extracts the entire buffer
	 * @return The entire buffer as a byte array
	 */
	public byte[] extractAll() {
		byte[] outData = new byte[offset_pointer];
		for (int i = 0; i < offset_pointer; i++)
			outData[i] = data.poll();
		offset_pointer = 0;
		return outData;
	}
}
