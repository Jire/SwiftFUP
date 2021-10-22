package org.jire.swiftfup.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Jire
 */
public interface SwiftFUP {
	
	FileRequests getFileRequests();
	
	void setFileRequests(FileRequests fileRequests);
	
	FileClient getFileClient();
	
	void setFileClient(FileClient fileClient);
	
	FileStore getFileStore();
	
	FileDecompressedListener getFileDecompressedListener();
	
	AutoProcessor getAutoProcessor();
	
	default int getExpectedRequests() {
		return 8192;
	}
	
	default void startAutoProcessor() {
		AutoProcessor autoProcessor = getAutoProcessor();
		if (autoProcessor != null)
			new Thread(() -> autoProcessor.autoProcessLoop(getFileRequests())).start();
	}
	
	default FileRequests initializeFileRequests() {
		FileRequests fileRequests = new FileRequests(getExpectedRequests(), getFileStore(), getFileDecompressedListener());
		setFileRequests(fileRequests);
		return fileRequests;
	}
	
	default FileClient initializeFileClient(String host, int port) {
		FileClient fileClient = new FileClient(host, port, getFileRequests());
		setFileClient(fileClient);
		return fileClient;
	}
	
	default void initializeSwiftFUP(String host, int port) {
		initializeFileRequests();
		FileClient fileClient = initializeFileClient(host, port);
		fileClient.connect();
		
		startAutoProcessor();
		initializeChecksums();
	}
	
	default FileChecksumsResponse initializeChecksums() {
		return initializeChecksums(getFileClient(), getDefaultChecksumsTimeout(), getDefaultChecksumsTimeoutUnit());
	}
	
	default int getDefaultChecksumsTimeout() {
		return 30;
	}
	
	default TimeUnit getDefaultChecksumsTimeoutUnit() {
		return TimeUnit.SECONDS;
	}
	
	default FileChecksumsResponse initializeChecksums(FileClient fileClient,
	                                                  int timeout, TimeUnit timeoutTimeUnit) {
		FileChecksumsRequest fileChecksumsRequest = fileClient.requestChecksums();
		fileClient.flush();
		try {
			return fileChecksumsRequest.get(timeout, timeoutTimeUnit);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}
	
}
