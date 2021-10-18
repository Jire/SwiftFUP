package org.jire.swiftfp.client.net;

import org.jire.swiftfp.client.FileChecksumRequester;
import org.jire.swiftfp.client.FileRequester;
import org.jire.swiftfp.client.FileResponse;

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
