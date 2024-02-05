package org.jire.swiftfup.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author Jire
 */
public enum AutoProcessors implements AutoProcessor {

    SLOW(TimeUnit.MILLISECONDS.toNanos(100)),
    FAST(TimeUnit.MILLISECONDS.toNanos(20));

    private static final Logger logger = LoggerFactory.getLogger(AutoProcessors.class);

    private final long sleepTimeTargetNanos;

    AutoProcessors(long sleepTimeTargetNanos) {
        this.sleepTimeTargetNanos = sleepTimeTargetNanos;
    }

    @Override
    public void autoProcess(FileClient fileClient, FileRequests fileRequests) {
        long start = System.nanoTime();

        try {
            fileRequests.process(fileClient);
        } catch (Exception e) {
            logger.error("Failed to process file requests", e);
        }

        long end = System.nanoTime();

        long elapsed = end - start;

        long sleepTimeNanos = sleepTimeTargetNanos - elapsed;
        long sleepTimeMillis = TimeUnit.NANOSECONDS.toMillis(sleepTimeNanos);

        if (sleepTimeMillis > 0) {
            try {
                Thread.sleep(sleepTimeMillis);
            } catch (InterruptedException ie) {
                logger.error("Auto process was interrupted during sleep of "
                                + sleepTimeMillis + "ms (target was " + sleepTimeTargetNanos + "ns)",
                        ie);
            }
        }
    }

}
