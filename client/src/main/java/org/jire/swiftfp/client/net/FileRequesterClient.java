package org.jire.swiftfp.client.net;

import org.jire.swiftfp.client.FilePair;
import org.jire.swiftfp.client.FileRequest;
import org.jire.swiftfp.client.FileRequester;
import org.jire.swiftfp.client.FileResponse;

import java.util.function.Consumer;

/**
 * @author Jire
 */
public interface FileRequesterClient
		<RESPONSE extends FileResponse,
				REQUESTER extends FileRequester<RESPONSE>>
		extends Client {
	
	REQUESTER getFileRequester();
	
	void request(FileRequest<RESPONSE> request);
	
	Consumer<FileRequest<RESPONSE>> getWhenNew();
	
	default FileRequest<RESPONSE> request(int filePair) {
		return getFileRequester().request(filePair, getWhenNew());
	}
	
	default FileRequest<RESPONSE> request(int index, int file) {
		return request(FilePair.create(index, file));
	}
	
}
