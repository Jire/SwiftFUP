package org.jire.swiftfup.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Jire
 */
public abstract class AbstractFileRequester
		<RESPONSE extends FileResponse>
		implements FileRequester<RESPONSE> {
	
	private final Executor executor = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setName("File Requester");
		thread.setPriority(Thread.NORM_PRIORITY - 1);
		return thread;
	});
	
	private final Int2ObjectMap<FileRequest<RESPONSE>> requests;
	
	public AbstractFileRequester(int expected) {
		requests = new Int2ObjectOpenHashMap<>(expected);
	}
	
	@Override
	public FileRequest<RESPONSE> get(int filePair) {
		return requests.get(filePair);
	}
	
	@Override
	public void set(int filePair, FileRequest<RESPONSE> fileRequest) {
		requests.put(filePair, fileRequest);
	}
	
	@Override
	public Executor getExecutor() {
		return executor;
	}
	
}
