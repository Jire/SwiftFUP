package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public final class AutoProcessorThread extends Thread {

    private final FileClient fileClient;
    private final FileRequests fileRequests;

    private final AutoProcessor autoProcessor;

    public AutoProcessorThread(AutoProcessor autoProcessor,

                               FileClient fileClient,
                               FileRequests fileRequests,

                               String threadName,
                               int priority,
                               boolean daemon) {
        super(threadName);

        this.autoProcessor = autoProcessor;

        this.fileClient = fileClient;
        this.fileRequests = fileRequests;

        setPriority(priority);
        setDaemon(daemon);
    }

    public AutoProcessorThread(AutoProcessor autoProcessor,

                               FileClient fileClient,
                               FileRequests fileRequests) {
        this(autoProcessor,
                fileClient, fileRequests,
                "SwiftFUP-AutoProcessor",
                Thread.NORM_PRIORITY + 1,
                true);
    }

    @Override
    public void run() {
        while (!interrupted()) {
            autoProcessor.autoProcess(fileClient, fileRequests);
        }
    }

}
