package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public interface FileStore {
	
	FileIndex getIndex(int indexID);
	
	byte[] decompress(byte[] data);
	
}
