/*
package org.jire.swiftfp.client;

*/
/**
 * @author Jire
 *//*

public final class FileRequestResponse {
	
	public final int filePair;
	public final int index, file;
	
	public final int dataSize;
	public final byte[] data;
	
	public final FileRequestResponseSupplier onDemandData;
	
	public FileRequestResponse(int filePair, int index, int file, int dataSize, byte[] data) {
		this.filePair = filePair;
		this.index = index;
		this.file = file;
		this.dataSize = dataSize;
		this.data = data;
		
		if (data != null && index > 0) {
			onDemandData = new OnDemandData();
			onDemandData.dataType = index - 1;
			onDemandData.ID = file;
			onDemandData.buffer = Client.onDemandFetcher.decompress(data);
			onDemandData.incomplete = false;
		} else onDemandData = null;
	}
	
	public byte[] copyData() {
		if (data == null) return null;
		
		byte[] copy = new byte[data.length];
		System.arraycopy(data, 0, copy, 0, copy.length);
		return copy;
	}
	
}
*/
