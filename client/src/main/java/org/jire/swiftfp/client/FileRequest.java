package org.jire.swiftfp.client;

import java.util.concurrent.CompletableFuture;

/**
 * @author Jire
 */
public final class FileRequest<RESPONSE extends FileResponse> extends CompletableFuture<RESPONSE> {
	
	private final int filePair;
	
	public FileRequest(int filePair) {
		this.filePair = filePair;
	}
	
	public int getFilePair() {
		return filePair;
	}
	
}
