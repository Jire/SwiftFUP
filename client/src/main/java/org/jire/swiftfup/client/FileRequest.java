package org.jire.swiftfup.client;

import java.util.concurrent.CompletableFuture;

/**
 * @author Jire
 */
public final class FileRequest extends CompletableFuture<FileResponse> {

	private boolean sent;

	public void sent() {
		this.sent = true;
	}

	public boolean isSent() {
		return sent;
	}

	private final int filePair;
	
	public FileRequest(int filePair) {
		this.filePair = filePair;
	}
	
	public int getFilePair() {
		return filePair;
	}
	
}
