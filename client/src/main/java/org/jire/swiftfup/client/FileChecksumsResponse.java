package org.jire.swiftfup.client;

import it.unimi.dsi.fastutil.ints.Int2IntMap;

/**
 * @author Jire
 */
public final class FileChecksumsResponse {
	
	private final Int2IntMap fileToChecksum;
	
	public FileChecksumsResponse(Int2IntMap fileToChecksum) {
		this.fileToChecksum = fileToChecksum;
	}
	
	public Int2IntMap getFileToChecksum() {
		return fileToChecksum;
	}
	
}
