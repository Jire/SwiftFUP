package org.jire.swiftfp.client;

/**
 * @author Jire
 */
public enum FilePair {
	;
	
	public static int create(int index, int file) {
		if ((index & ~0x1F) != 0) throw new IllegalArgumentException("invalid index " + index + ":" + file);
		if ((file & ~0x7FFFF) != 0) throw new IllegalArgumentException("invalid file " + index + ":" + file);
		return (index & 0x1F) | ((file & 0x7FFFF) << 5);
	}
	
	public static int index(int pair) {
		return pair & 0x1F;
	}
	
	public static int file(int pair) {
		return (pair >>> 5) & 0x7FFFF;
	}
	
	public static String toString(int filePair) {
		return index(filePair) + ":" + file(filePair);
	}
	
}
