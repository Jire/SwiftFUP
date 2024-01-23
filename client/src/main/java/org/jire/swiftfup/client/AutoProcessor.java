package org.jire.swiftfup.client;

import java.util.concurrent.TimeUnit;

/**
 * @author Jire
 */
public interface AutoProcessor {

    default void autoProcess(FileClient fileClient,
                             FileRequests fileRequests) {
        long start = System.nanoTime();
        fileRequests.process(fileClient);
        long end = System.nanoTime();
        long elapsed = end - start;
        long sleepTime = sleepTime() - elapsed;
        long sleepTimeMillis = TimeUnit.NANOSECONDS.toMillis(sleepTime);
        if (sleepTimeMillis > 0) {
            try {
                Thread.sleep(sleepTimeMillis);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    default void autoProcessLoop(FileClient fileClient,
                                 FileRequests fileRequests) {
        while (!Thread.interrupted()) {
            autoProcess(fileClient, fileRequests);
        }
    }

    default int sleepTime() {
        return 20;
    }

}
