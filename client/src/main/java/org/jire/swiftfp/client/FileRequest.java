package org.jire.swiftfp.client;

import org.jire.swiftfp.client.FileRequestResponse;

import java.util.concurrent.CompletableFuture;

/**
 * @author Jire
 */
public final class FileRequest extends CompletableFuture<FileRequestResponse> {
	
	private final int filePair;
	private final boolean needsComplete;
	
	public FileRequest(int filePair, boolean needsComplete) {
		this.filePair = filePair;
		this.needsComplete = needsComplete;
	}
	
	public int getFilePair() {
		return filePair;
	}
	
	public boolean isNeedsComplete() {
		return needsComplete;
	}
	
}
