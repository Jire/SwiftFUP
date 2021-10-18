package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public interface FileChecksumRequester {
	
	int getChecksum(int filePair);
	
	void setChecksum(int filePair, int checksum);
	
	FileChecksumsRequest requestChecksums();
	
	boolean checksumMatches(int checksum, byte[] buffer);
	
	boolean checksumMatches(int filePair, int checksum);
	
	boolean completeChecksumsRequest(FileChecksumsResponse checksumsResponse);
	
}
