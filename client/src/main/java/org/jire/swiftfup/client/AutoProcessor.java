package org.jire.swiftfup.client;

/**
 * @author Jire
 */
public interface AutoProcessor {
	
	boolean shouldAutoProcess(FileRequests fileRequests);
	
	default void autoProcess(FileRequests fileRequests) {
		long start = System.currentTimeMillis();
		fileRequests.process();
		long end = System.currentTimeMillis();
		long elapsed = start - end;
		long sleepTime = sleepTime() - elapsed;
		if (sleepTime > 0) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
				return;
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
