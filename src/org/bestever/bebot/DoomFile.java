package org.bestever.bebot;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class encapsulated wads, but also has methods for PK3's
 */
public class DoomFile {
	
	public String headerType;
	public int headerTotalLumps;
	public int headerPointerToDirectory;
	public int[] fileOffset;
	public int[] fileSize;
	public String[] lumpName;
	public String[] levelNames;
	public static final int LOCATION_NOT_FOUND = -1;
	public static final String[] lumpMapNames = { "THINGS", "LINEDEFS", "SIDEDEFS", "VERTEXES", "SEGS", "SSECTORS", "REJECT", "BLOCKMAP", "GL_VERT", "GL_SEGS", "GL_SSECT", "GL_NODES" };

	public DoomFile (String path) throws IOException
	{
		byte[] wadData = Utility.getByteArrayFromFile(path);

		this.headerType = Utility.bytesToString(Arrays.copyOfRange(wadData, 0, 4));
		this.headerTotalLumps = Utility.bytesToInt(wadData[4], wadData[5], wadData[6], wadData[7]);
		this.headerPointerToDirectory = Utility.bytesToInt(wadData[8], wadData[9], wadData[10], wadData[11]);
		System.out.println("Wad data: " + this.headerType + ", " + this.headerTotalLumps + " total lumps, " + this.headerPointerToDirectory + " directory offset");

		parseDirectory(wadData);
		getLevelNames(wadData);

		System.gc();
	}
	
	/**
	 * Given the path to a pk3, this function will open the pk3 file and get the maps.
	 * @param pathToFile The path to (and including) the file
	 * @return A String designed for running (ex: "+addmap zdmap01 +addmap zdmap02") 
	 */
	public static String getPK3MapNames(String pathToFile) {
		ZipFile zip;
		try {
			zip = new ZipFile(pathToFile);
		} catch (IOException e2) {
			e2.printStackTrace();
			return null;
		}
		Enumeration<? extends ZipEntry> e = zip.entries();
		String mapNames = "";
		while (e.hasMoreElements()) {
			ZipEntry ze = (ZipEntry)e.nextElement();
			String temp = "";
			if ((!ze.isDirectory()) && (ze.getName().toLowerCase().startsWith("maps/"))) {
				temp = ze.getName().substring(5);
				mapNames += "+addmap " + temp.substring(0, temp.length() - 4) + " ";
			}
		}
		try {
			zip.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return mapNames;
	}

	private void parseDirectory(byte[] wadData)
	{
		this.fileOffset = new int[this.headerTotalLumps];
		this.fileSize = new int[this.headerTotalLumps];
		this.lumpName = new String[this.headerTotalLumps];
		int c = 0;
		System.out.println("Pointer: " + this.headerPointerToDirectory + ", total length: " + wadData.length + ", difference = " + (wadData.length - this.headerPointerToDirectory));
		for (int off = this.headerPointerToDirectory; off < wadData.length; off += 16) {
			this.fileOffset[c] = Utility.bytesToInt(wadData[off], wadData[(off + 1)], wadData[(off + 2)], wadData[(off + 3)]);
			this.fileSize[c] = Utility.bytesToInt(wadData[(off + 4)], wadData[(off + 5)], wadData[(off + 6)], wadData[(off + 7)]);
			this.lumpName[c] = new String(Arrays.copyOfRange(wadData, off + 8, off + 16)).trim();
			c++;
		}
	}

	public int findLumpLocation(String lumpname)
	{
		String name = lumpname.toUpperCase();
		int index = -1;
		for (int i = 0; i < this.lumpName.length; i++) {
			if (this.lumpName[i].equals(name)) {
				index = i;
				break;
			}
		}
		if (index != -1)
			return this.fileOffset[index];
		System.out.println("Could not find lump name = " + lumpname);
		return -1;
	}

	@SuppressWarnings("all")
	public void getLevelNames(byte[] wadData)
	{
		String[] temp = new String[this.lumpName.length];
		Arrays.fill(temp, "");
		int tempIndex = 0;
		List listMapNames = Arrays.asList(lumpMapNames);
		for (int i = 0; i < this.lumpName.length; i++)
		{
			if ((i != this.lumpName.length - 1) && (this.fileSize[i] == 0) && (listMapNames.contains(this.lumpName[(i + 1)])) && (!listMapNames.contains(this.lumpName[i])) && (!this.lumpName[i].startsWith("GL_"))) {
				temp[tempIndex] = this.lumpName[i];
				tempIndex++;
			}
		}
		if (tempIndex == 0) {
			this.levelNames = new String[0];
			return;
		}
		this.levelNames = new String[tempIndex];
		for (int j = 0; j < tempIndex; j++) {
			this.levelNames[j] = temp[j];
		}
		Arrays.sort(this.levelNames);
		System.gc();
	}

	public void outputData()
	{
		if (this.fileOffset == null) {
			System.out.println("Cannot print data since fileOffset is null");
			return;
		}
		for (int i = 0; i < this.fileOffset.length; i++)
			System.out.println(this.lumpName[i] + "\n" + this.fileOffset[i] + "\n" + this.fileSize[i] + "\n");
	}
}
