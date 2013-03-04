package org.bestever.bebot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class ReverseLineInputStream extends InputStream {

    RandomAccessFile in;

    long currentLineStart = -1;
    long currentLineEnd = -1;
    long currentPos = -1;
    long lastPosInFile = -1;

    public ReverseLineInputStream(File file) throws FileNotFoundException {
        in = new RandomAccessFile(file, "r");
        currentLineStart = file.length();
        currentLineEnd = file.length();
        lastPosInFile = file.length() -1;
        currentPos = currentLineEnd; 
    }

    public void findPrevLine() throws IOException {

        currentLineEnd = currentLineStart; 

        // Beginning of file
        if (currentLineEnd == 0) {
            currentLineEnd = -1;
            currentLineStart = -1;
            currentPos = -1;
            return; 
        }

        long filePointer = currentLineStart -1;

         while (true) {
             filePointer--;

            // First line of the file
            if (filePointer < 0) {  
                break; 
            }

            in.seek(filePointer);
            int readByte = in.readByte();

            // Ignore the last LF and search back to find the previous LF
            if (readByte == 0xA && filePointer != lastPosInFile ) {   
                break;
            }
         }
         // Start at pointer + 1  
         currentLineStart = filePointer + 1;
         currentPos = currentLineStart;
    }

    public int read() throws IOException {

        if (currentPos < currentLineEnd ) {
            in.seek(currentPos++);
            int readByte = in.readByte();
            return readByte;
        }
        
        else if (currentPos < 0) {
            return -1;
        }
        
        else {
            findPrevLine();
            return read();
        }
    }
}