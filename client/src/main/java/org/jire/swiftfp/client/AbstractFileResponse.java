package org.jire.swiftfp.client;

/**
 * @author Jire
 */
public abstract class AbstractFileResponse implements FileResponse {
	
	private final int filePair;
	
	public AbstractFileResponse(int filePair) {
		this.filePair = filePair;
	}
	
	@Override
	public int getFilePair() {
		return filePair;
	}
	
}
