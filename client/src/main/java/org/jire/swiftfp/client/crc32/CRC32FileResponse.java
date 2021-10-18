package org.jire.swiftfp.client.crc32;

import org.jire.swiftfp.client.FilePair;
import org.jire.swiftfp.client.FileResponse;
import org.jire.swiftfp.client.FileStore;

/**
 * @author Jire
 */
public interface CRC32FileResponse extends FileResponse {
	
	int getDataSize();
	
	byte[] getData();
	
	default byte[] getDataCopy() {
		byte[] data = getData();
		if (data == null) return null;
		
		int dataSize = getDataSize();
		byte[] copy = new byte[dataSize];
		System.arraycopy(data, 0, copy, 0, dataSize);
		return copy;
	}
	
	void markLoadedData();
	
	FileStore getFileStore(int index);
	
	default void writeToStore() {
		FileStore store = getFileStore(getIndex());
		if (store == null)
			throw new NullPointerException("File store " + getIndex() + " did not exist!");
		store.write(getFile(), getData(), getDataSize());
	}
	
	@Override
	default void complete() {
		if (FilePair.index(getFilePair()) > 0) markLoadedData();
		writeToStore();
	}
	
}
