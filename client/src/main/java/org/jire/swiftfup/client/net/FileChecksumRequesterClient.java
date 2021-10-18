package org.jire.swiftfup.client.net;

import org.jire.swiftfup.client.FileChecksumRequester;
import org.jire.swiftfup.client.FileChecksumsRequest;

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
