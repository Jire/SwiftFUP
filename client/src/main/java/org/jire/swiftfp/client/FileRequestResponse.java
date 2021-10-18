package org.jire.swiftfp.client;

/**
 * @author Jire
 */
public interface FileRequestResponse {
	
	int getFilePair();
	
	default int getIndex() {
		return FilePair.index(getFilePair());
	}
	
	default int getFile() {
		return FilePair.file(getFilePair());
	}
	
	int getDataSize();
	
	byte[] getData();
	
}
