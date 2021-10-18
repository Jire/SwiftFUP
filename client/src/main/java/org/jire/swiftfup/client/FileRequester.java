package org.jire.swiftfup.client;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author Jire
 */
public interface FileRequester<RESPONSE extends FileResponse> extends FileResponder<RESPONSE> {
	
	Executor getExecutor();
	
	FileRequest<RESPONSE> get(int filePair);
	
	void set(int filePair, FileRequest<RESPONSE> fileRequest);
	
	default FileRequest<RESPONSE> newRequest(int filePair) {
		final FileRequest<RESPONSE> request = new FileRequest<>(filePair);
		request.thenAcceptAsync(response -> {
			try {
				response.complete();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}, getExecutor());
		return request;
	}
	
	default boolean complete(RESPONSE response) {
		int filePair = response.getFilePair();
		
		FileRequest<RESPONSE> fileRequest = get(filePair);
		if (fileRequest == null)
			throw new NullPointerException("File request did not exist! (" + FilePair.toString(filePair) + ")");
		if (fileRequest.isDone())
			throw new IllegalStateException("File request is already completed! (" + FilePair.toString(filePair) + ")");
		
		return fileRequest.complete(response);
	}
	
	default FileRequest<RESPONSE> request(int filePair, Consumer<FileRequest<RESPONSE>> whenNew) {
		final FileRequest<RESPONSE> existingRequest = get(filePair);
		if (existingRequest != null) return existingRequest;
		
		FileRequest<RESPONSE> newRequest = newRequest(filePair);
		set(filePair, newRequest);
		
		if (whenNew != null) whenNew.accept(newRequest);
		
		return newRequest;
	}
	
}
