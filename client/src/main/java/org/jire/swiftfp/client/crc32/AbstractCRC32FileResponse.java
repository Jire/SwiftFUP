package org.jire.swiftfp.client.crc32;

import org.jire.swiftfp.client.AbstractFileResponse;

/**
 * @author Jire
 */
public abstract class AbstractCRC32FileResponse
		extends AbstractFileResponse
		implements CRC32FileResponse {
	
	private final int dataSize;
	private final byte[] data;
	
	public AbstractCRC32FileResponse(int filePair,
	                                 int dataSize, byte[] data) {
		super(filePair);
		
		this.dataSize = dataSize;
		this.data = data;
	}
	
	@Override
	public int getDataSize() {
		return dataSize;
	}
	
	@Override
	public byte[] getData() {
		return data;
	}
	
}
