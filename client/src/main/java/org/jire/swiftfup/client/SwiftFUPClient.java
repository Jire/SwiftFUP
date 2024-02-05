package org.jire.swiftfup.client;

import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Jire
 */
public interface SwiftFUPClient {

    FileRequests getFileRequests();

    void setFileRequests(FileRequests fileRequests);

    FileClient getFileClient();

    void setFileClient(FileClient fileClient);

    FileStore getFileStore();

    FileDecompressedListener getFileDecompressedListener();

    default AutoProcessor getAutoProcessor() {
        return SwiftFUP.DEFAULT_AUTO_PROCESSOR;
    }

    default int getExpectedRequests() {
        return SwiftFUP.DEFAULT_EXPECTED_REQUESTS;
    }

    default void startAutoProcessor() {
        AutoProcessor autoProcessor = getAutoProcessor();
        if (autoProcessor != null) {
            AutoProcessorThread autoProcessorThread = new AutoProcessorThread(autoProcessor,
                    getFileClient(),
                    getFileRequests());
            autoProcessorThread.start();
        }
    }

    default FileRequests initializeFileRequests() {
        FileRequests fileRequests = new FileRequests(
                getExpectedRequests(),
                getFileStore(),
                getFileDecompressedListener());
        setFileRequests(fileRequests);
        return fileRequests;
    }

    default FileClient initializeFileClient(FileRequests fileRequests,

                                            FileClientGroup fileClientGroup,
                                            Runnable whileWaiting,
                                            SocketAddress... remoteAddresses) {
        FileClient fileClient = fileClientGroup.createClient(fileRequests, remoteAddresses);
        fileClient.connect(false, whileWaiting);
        setFileClient(fileClient);
        return fileClient;
    }

    default void initializeSwiftFUP(FileClientGroup fileClientGroup,
                                    SocketAddress... remoteAddresses) {
        FileRequests fileRequests = initializeFileRequests();
        initializeFileClient(fileRequests, fileClientGroup, null, remoteAddresses);

        startAutoProcessor();

        initializeChecksums();
    }

    default FileChecksumsResponse initializeChecksums() {
        return initializeChecksums(getFileClient(), getDefaultChecksumsTimeout(), getDefaultChecksumsTimeoutUnit());
    }

    default int getDefaultChecksumsTimeout() {
        return SwiftFUP.DEFAULT_CHECKSUMS_TIMEOUT;
    }

    default TimeUnit getDefaultChecksumsTimeoutUnit() {
        return SwiftFUP.DEFAULT_CHECKSUMS_TIMEOUT_UNIT;
    }

    default FileChecksumsResponse initializeChecksums(FileClient fileClient,
                                                      int timeout, TimeUnit timeoutTimeUnit) {
        FileChecksumsRequest fileChecksumsRequest = fileClient.requestChecksums();
        try {
            return fileChecksumsRequest.get(timeout, timeoutTimeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

}
