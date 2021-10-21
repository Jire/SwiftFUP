package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public final class FileResponse {
	
	private final int filePair;
	
	private final byte[] data;
	
	private volatile byte[] decompressedData;
	
	public FileResponse(int filePair, byte[] data) {
		this.filePair = filePair;
		this.data = data;
	}
	
	public int getFilePair() {
		return filePair;
	}
	
	public int getIndex() {
		return FilePair.index(filePair);
	}
	
	public int getFile() {
		return FilePair.file(filePair);
	}
	
	public byte[] getData() {
		return data;
	}
	
	public byte[] getDecompressedData() {
		return decompressedData;
	}
	
	public void setDecompressedData(byte[] decompressedData) {
		this.decompressedData = decompressedData;
	}
	
}
