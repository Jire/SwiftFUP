package org.jire.swiftfup.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jire
 */
public final class FileResponse {

	private static final Logger logger = LoggerFactory.getLogger(FileResponse.class);

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

	public byte[] setDecompressedData(FileStore fileStore) {
		byte[] data = getData();
		if (data == null || data.length < 1) return null;

		byte[] decompressedData = null;
		try {
			decompressedData = fileStore.decompress(data);
		} catch (Exception e) {
			logger.error("Failed to decompress data of file pair (" + FilePair.toString(filePair) + ")", e);
		}

		this.decompressedData = decompressedData;
		return decompressedData;
	}

}
