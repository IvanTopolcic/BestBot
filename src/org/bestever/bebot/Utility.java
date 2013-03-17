package org.bestever.bebot;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class was written a very long time ago and probably has some outdated methods, or better
 * ways of doing things. Right now it is left as is since it's functional, but in the future it
 * could be re-written.
 */
public class Utility {
	
	public static final short BYTE_MAX = 256;
	public static final int SHORT_MAX = 65536;

	public static byte[] getByteArrayFromFile(String filePath) throws IOException {
		RandomAccessFile f = new RandomAccessFile(filePath, "r");
		byte[] wadData = new byte[(int)f.length()];
		f.read(wadData);
		f.close();
		return wadData;
	}

	public static short bytesToShort(byte lowerHalf, byte biggerHalf, boolean littleEndian) {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		bb.put(lowerHalf);
		bb.put(biggerHalf);
		return bb.getShort(0);
	}

	public static short bytesToShort(byte lowerHalf, byte biggerHalf) {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(lowerHalf);
		bb.put(biggerHalf);
		return bb.getShort(0);
	}

	public static int bytesToInt(byte lowest, byte lower, byte high, byte highest) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(lowest);
		bb.put(lower);
		bb.put(high);
		bb.put(highest);
		return bb.getInt(0);
	}

	public static int bytesToInt(byte lowest, byte lower, byte high, byte highest, boolean littleEndian) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		bb.put(lowest);
		bb.put(lower);
		bb.put(high);
		bb.put(highest);
		return bb.getInt(0);
	}

	public static int bytesToInt(byte[] b) {
		if (b.length != 4) {
			System.out.println("bytesToInt had a length that was not equal to four");
			return 0;
		}
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(b[0]);
		bb.put(b[1]);
		bb.put(b[2]);
		bb.put(b[3]);
		return bb.getInt(0);
	}

	public static int bytesToInt(byte[] b, boolean littleEndian) {
		if (b.length != 4) {
			System.out.println("bytesToInt had a length that was not equal to four");
			return 0;
		}
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		bb.put(b[0]);
		bb.put(b[1]);
		bb.put(b[2]);
		bb.put(b[3]);
		return bb.getInt(0);
	}

	public static int bytesToInt(byte[] b, int startIndex) {
		if (b.length - 4 <= startIndex) {
			System.out.println("bytesToInt had an index that would cause an ArrayOutOfBoundsException");
			return 0;
		}
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(b[0]);
		bb.put(b[1]);
		bb.put(b[2]);
		bb.put(b[3]);
		return bb.getInt(0);
	}

	public static String bytesToString(byte[] b, boolean littleEndian) {
		ByteBuffer bb = ByteBuffer.allocate(b.length);
		bb.order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		for (int i = 0; i < b.length; i++)
			bb.put(b[i]);
		return bb.toString();
	}

	public static String bytesToString(byte[] b) {
		return new String(b);
	}

	public static int[] convertByteArrayToIntArray(byte[] b) {
		int[] temp = new int[b.length];
		for (int i = 0; i < b.length; i++)
			temp[i] = b[i];
		return temp;
	}

	public static int[] convertByteArrayToIntArrayUnsigned(byte[] b) {
		int[] temp = new int[b.length];
		for (int i = 0; i < b.length; i++)
			if (b[i] < 0)
				b[i] += 256;
			else
				temp[i] = b[i];
		return temp;
	}
}
