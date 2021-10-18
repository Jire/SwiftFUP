package org.jire.swiftfup.client.net;

import org.jire.swiftfup.client.FileChecksumRequester;
import org.jire.swiftfup.client.FileRequester;
import org.jire.swiftfup.client.FileResponse;

/**
 * @author Jire
 */
public interface FileClient
		<RESPONSE extends FileResponse,
				REQUESTER extends FileRequester<RESPONSE> & FileChecksumRequester>
		extends FileRequesterClient<RESPONSE, REQUESTER>,
		FileChecksumRequesterClient {
	
	REQUESTER getFileRequester();
	
	default FileChecksumRequester getFileChecksumRequester() {
		return getFileRequester();
	}
	
}
