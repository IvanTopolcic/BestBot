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
	
	/**
	 * Signifies what the header is (IWAD or PWAD)
	 */
	public String headerType;
	
	/**
	 * Contains the number of lumps in the file
	 */
	public int headerTotalLumps;
	
	/**
	 * Contains a pointer (offset) to the directory of the wad
	 */
	public int headerPointerToDirectory;
	
	/**
	 * Shows the file offset for all the wad files
	 */
	public int[] fileOffset;
	
	/**
	 * Contains the filesize for the files at each offset
	 */
	public int[] fileSize;
	
	/**
	 * An index of all the lump names
	 */
	public String[] lumpName;
	
	/**
	 * An index of the level names
	 */
	public String[] levelNames;
	
	/**
	 * If theres a location finding error
	 */
	public static final int LOCATION_NOT_FOUND = -1;
	
	/**
	 * Common map lumps that are required are enumerated here, added support for UDMF
	 */
	public static final String[] lumpMapNames = { "THINGS", "LINEDEFS", "SIDEDEFS", "VERTEXES", "SEGS", "SSECTORS", "REJECT", "BLOCKMAP", "GL_VERT", "GL_SEGS", "GL_SSECT", "GL_NODES", "TEXTMAP", "ZNODES", "DIALOGUE", "ENDMAP" };

	/**
	 * Accepts a path to the wad file and will parse it upon invoking the constructor
	 * @param path String path to a file
	 * @throws IOException If there is a problem with reading the file
	 */
	public DoomFile (String path) throws IOException {
		byte[] wadData = Utility.getByteArrayFromFile(path);
		this.headerType = Utility.bytesToString(Arrays.copyOfRange(wadData, 0, 4));
		this.headerTotalLumps = Utility.bytesToInt(wadData[4], wadData[5], wadData[6], wadData[7]);
		this.headerPointerToDirectory = Utility.bytesToInt(wadData[8], wadData[9], wadData[10], wadData[11]);
		System.out.println("Wad data: " + this.headerType + ", " + this.headerTotalLumps + " total lumps, " + this.headerPointerToDirectory + " directory offset");
		parseDirectory(wadData);
		parseLevelNames(wadData);
		System.gc(); // There may be a fair amount of junk to remove after getting the level names
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

	/**
	 * Takes the byte data and parses the directory
	 * @param wadData The files data in bytes
	 */
	private void parseDirectory(byte[] wadData) {
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

	/**
	 * Will search through the wad to find the offset of the lump's location
	 * @param lumpname The name of the lump to find (it is converted to uppercase in the function)
	 * @return The index of the lump
	 */
	public int findLumpLocation(String lumpname) {
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

	/**
	 * This goes through and gets the level names from the wad's data
	 * @param wadData The data of the wad
	 */
	private void parseLevelNames(byte[] wadData) {
		String[] temp = new String[this.lumpName.length];
		Arrays.fill(temp, "");
		int tempIndex = 0;
		List<String> listMapNames = Arrays.asList(lumpMapNames);
		for (int i = 0; i < this.lumpName.length; i++) {
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
		for (int j = 0; j < tempIndex; j++)
			this.levelNames[j] = temp[j];
		Arrays.sort(this.levelNames);
	}
	
	/**
	 * This method will return the level names in a nice string format
	 * @param appendAddmap If the user wants +addmap [maphere] in front of them for creating servers
	 * @return The string of all the level names
	 */
	public String getLevelNames(boolean appendAddmap) {
		String output = "";
		for (String s : levelNames)
			if (s != null)
				output += (appendAddmap ? "+addmap " : "") + s + " ";
		return output;
	}
	
	/**
	 * Nulls everything in an attempt to prepare it for garbage collection
	 */
	public void prepareForGarbageCollection() {
		this.headerType = null;
		this.fileOffset = null;
		this.fileSize = null;
		this.lumpName = null;
		this.levelNames = null;
	}

	/**
	 * This is a debug function
	 */
	public void outputData() {
		if (this.fileOffset == null) {
			System.out.println("Cannot print data since fileOffset is null");
			return;
		}
		for (int i = 0; i < this.fileOffset.length; i++)
			System.out.println(this.lumpName[i] + "\n" + this.fileOffset[i] + "\n" + this.fileSize[i] + "\n");
	}
}
