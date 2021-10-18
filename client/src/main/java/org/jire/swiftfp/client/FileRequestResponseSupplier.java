package org.jire.swiftfp.client;

/**
 * @author Jire
 */
@FunctionalInterface
public interface FileRequestResponseSupplier {
	
	FileRequestResponse supply(int filePair, byte[] data, int dataSize);
	
	default FileRequestResponse supply(int filePair, byte[] data) {
		return supply(filePair, data, data.length);
	}
	
}
