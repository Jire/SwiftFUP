package org.jire.swiftfp.client.net;

import org.jire.swiftfp.client.FileChecksumRequester;
import org.jire.swiftfp.client.FileChecksumsRequest;

/**
 * @author Jire
 */
public interface FileChecksumRequesterClient extends Client {
	
	FileChecksumRequester getFileChecksumRequester();
	
	void requestChecksums(FileChecksumsRequest request);
	
	default FileChecksumsRequest requestChecksums() {
		FileChecksumsRequest request = getFileChecksumRequester().requestChecksums();
		if (!request.isDone())
			requestChecksums(request);
		return request;
	}
	
}
