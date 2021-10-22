package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public interface FileStore {
	
	FileIndex getIndex(int indexID);
	
	default byte[] decompress(byte[] data) {
		return GZIPDecompressor.getInstance().decompress(data);
	}
	
}
