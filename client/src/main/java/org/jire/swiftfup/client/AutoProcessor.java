package org.jire.swiftfup.client;

import java.util.concurrent.TimeUnit;

/**
 * @author Jire
 */
public interface AutoProcessor {
	
	boolean shouldAutoProcess(FileRequests fileRequests);

	default void autoProcess(FileRequests fileRequests) {
		long start = System.nanoTime();
		fileRequests.process();
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
	
	default void autoProcessLoop(FileRequests fileRequests) {
		while (shouldAutoProcess(fileRequests)) {
			autoProcess(fileRequests);
		}
	}
	
	default int sleepTime() {
		return 20;
	}
	
}
