package org.jire.swiftfup.client.crc32;

import org.jire.swiftfup.client.FileResponder;

/**
 * @author Jire
 */
public interface CRC32FileResponder
		<RESPONSE extends CRC32FileResponse>
		extends FileResponder<RESPONSE> {
	
	default boolean complete(int filePair, byte[] data, int dataSize) {
		return complete(newResponse(filePair, data, dataSize));
	}
	
	RESPONSE newResponse(int filePair, byte[] data, int dataSize);
	
	default RESPONSE newResponse(int filePair, byte[] data) {
		return newResponse(filePair, data, data.length);
	}
	
}
