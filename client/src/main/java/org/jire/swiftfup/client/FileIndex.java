package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public interface FileIndex {
	
	byte[] getFile(int fileID);
	
	void writeFile(int fileID, byte[] data);
	
}
