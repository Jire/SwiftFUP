package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public interface FileResponse {
	
	int getFilePair();
	
	default int getIndex() {
		return FilePair.index(getFilePair());
	}
	
	default int getFile() {
		return FilePair.file(getFilePair());
	}
	
	void complete();
	
}
