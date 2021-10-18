package org.jire.swiftfp.client;

/**
 * @author Jire
 */
public interface FileStore {
	
	byte[] read(int file);
	
	void write(int file, byte[] data, int dataSize);
	
	default void write(int file, byte[] data) {
		write(file, data, data.length);
	}
	
}
