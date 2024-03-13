package org.jire.swiftfup.client;

import io.netty.channel.Channel;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.function.Consumer;

/**
 * @author Jire
 */
public final class SwiftFUP {

    public static final int VERSION = 3;

    public static long getTimestamp() {
        return System.nanoTime();
    }

    public static final AutoProcessor DEFAULT_AUTO_PROCESSOR = AutoProcessors.FAST;

    public static final int DEFAULT_EXPECTED_REQUESTS = 2 << 18;

    public static SwiftFUP create(FileStore fileStore,
                                  FileDecompressedListener fileDecompressedListener,
                                  AutoProcessor autoProcessor,
                                  int expectedRequests) {
        return new SwiftFUP(fileStore, fileDecompressedListener, autoProcessor, expectedRequests);
    }

    public static SwiftFUP create(FileStore fileStore, FileDecompressedListener fileDecompressedListener) {
        return create(fileStore, fileDecompressedListener, DEFAULT_AUTO_PROCESSOR, DEFAULT_EXPECTED_REQUESTS);
    }

    private final FileStore fileStore;
    private final FileDecompressedListener fileDecompressedListener;
    private final AutoProcessor autoProcessor;
    private final int expectedRequests;

    private FileRequests fileRequests;
    private FileClient fileClient;

    private SwiftFUP(FileStore fileStore,
                     FileDecompressedListener fileDecompressedListener,
                     AutoProcessor autoProcessor,
                     int expectedRequests) {
        this.fileStore = fileStore;
        this.fileDecompressedListener = fileDecompressedListener;
        this.autoProcessor = autoProcessor;
        this.expectedRequests = expectedRequests;
    }

    public FileStore getFileStore() {
        return fileStore;
    }

    public FileDecompressedListener getFileDecompressedListener() {
        return fileDecompressedListener;
    }

    public AutoProcessor getAutoProcessor() {
        return autoProcessor;
    }

    public int getExpectedRequests() {
        return expectedRequests;
    }

    public FileRequests getFileRequests() {
        return fileRequests;
    }

    public FileClient getFileClient() {
        return fileClient;
    }

    public void startAutoProcessor() {
        final AutoProcessor autoProcessor = this.autoProcessor;
        if (autoProcessor != null) {
            new AutoProcessorThread(autoProcessor, fileClient, fileRequests)
                    .start();
        }
    }

    public FileRequests initializeFileRequests() {
        return fileRequests = new FileRequests(expectedRequests, fileStore, fileDecompressedListener);
    }

    public FileClient initializeFileClient(FileClientGroup fileClientGroup,
                                           @Nullable Runnable whileWaiting,
                                           @Nullable Consumer<Channel> whenReconnected,
                                           SocketAddress... remoteAddresses) {
        FileClient fileClient = fileClientGroup.createClient(fileRequests, whenReconnected, remoteAddresses);
        fileClient.connect(false, whileWaiting, whenReconnected);
        return this.fileClient = fileClient;
    }

}
