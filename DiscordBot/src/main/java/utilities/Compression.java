package utilities;

import java.io.IOException;

/**
 * 
 * Not used, was just thinking of data started getting too large where
 * processing on the cpu level was great but sending data was being a pain
 * 
 * Since we could package more data in a request since a request does not use
 * that much data so based off standard 1500 MTU for example we send information
 * that doesn't use (waste) all the data, so we could package / transaction more
 * data / command per packet
 * 
 * With compression if network was a bottleneck we could compress and save and
 * send alot more commands/data at a time if dezip is handled as well
 * 
 */
public class Compression {

	byte[] compressed;

	// return byte[]
	public static void compress(final String s) throws IOException {
		if (s.isEmpty() || s == null) {
			// return null;
		}
		/**
		 * 
		 * ByteArray GZIP write (make sure UTF-8 WITHOUT BOM also make sure system vs
		 * java) flush and close then return
		 * 
		 */
	}

	// return byte[]
	public static void decompress(final byte[] compressed) throws IOException {
		/**
		 * 
		 * StringBuilder for constant append Check for null & if compressed read it
		 * through the buffer as we append .toString() for text
		 * 
		 */
	}

	// helper method for checking compression gzip && checking the stream
}