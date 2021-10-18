package org.jire.swiftfp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jire.swiftfp.client.net.FileClientChannelInitializer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

/**
 * @author Jire
 */
public abstract class FileClient {
	
	private final String host;
	private final int port;
	
	private final ExecutorService storeService = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setName("Store Service");
		thread.setPriority(Thread.NORM_PRIORITY - 1);
		return thread;
	});
	
	private final ScheduledExecutorService flushService = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setName("Flush Service");
		return thread;
	});
	
	private final EventLoopGroup eventLoop =
			Epoll.isAvailable() ? new EpollEventLoopGroup(1)
					: KQueue.isAvailable() ? new KQueueEventLoopGroup(1)
					: new NioEventLoopGroup(1);
	private final Class<? extends Channel> channelClass =
			Epoll.isAvailable() ? EpollSocketChannel.class
					: KQueue.isAvailable() ? KQueueSocketChannel.class
					: NioSocketChannel.class;
	
	private final Int2IntMap checksums;
	private final Int2ObjectMap<FileRequest> fileRequests;
	
	private final ChannelHandler channelHandler = new FileClientChannelInitializer(this);
	private volatile Channel channel;
	private volatile FileChecksumsRequest fileChecksumsRequest;
	
	private final FileRequestResponseSupplier fileRequestResponseSupplier;
	
	public FileClient(String host, int port,
	                  FileRequestResponseSupplier fileRequestResponseSupplier,
	                  int indexSize,
	                  int expectedFileRequests) {
		this.host = host;
		this.port = port;
		
		this.fileRequestResponseSupplier = fileRequestResponseSupplier;
		
		checksums = new Int2IntOpenHashMap(indexSize);
		fileRequests = new Int2ObjectOpenHashMap<>(expectedFileRequests);
	}
	
	public FileChecksumsRequest requestIndexedChecksums() {
		if (channel != null && channel.isOpen())
			return fileChecksumsRequest;
		
		FileChecksumsRequest request = fileChecksumsRequest = new FileChecksumsRequest();
		Channel channel = this.channel = new Bootstrap()
				.channel(channelClass)
				.group(eventLoop)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(channelHandler)
				.connect(host, port)
				.syncUninterruptibly()
				.channel();
		
		channel.write(request, channel.voidPromise());
		
		flushService.scheduleAtFixedRate(() -> {
			if (channel.isOpen() && FileClient.this.channel == channel)
				channel.flush();
		}, 40, 40, TimeUnit.MILLISECONDS);
		
		return request;
	}
	
	public FileRequest getFileRequest(int filePair) {
		synchronized (fileRequests) {
			return fileRequests.get(filePair);
		}
	}
	
	public abstract FileStore getFileStore(int index);
	
	public void writeToStore(FileRequestResponse response) {
		FileStore store = getFileStore(response.getIndex());
		storeService.submit(() -> store.write(response.getFile(), response.getData(), response.getDataSize()));
	}
	
	public boolean completeFileRequest(FileRequestResponse response) {
		FileRequest fileRequest = getFileRequest(response.getFilePair());
		if (fileRequest.isDone())
			throw new RuntimeException("FILE ALREADY DONE! (" + response.getIndex() + ":" + response.getFile() + ")");
		
		return fileRequest.complete(response);
	}
	
	public abstract void markLoadedData(FileRequestResponse response);
	
	public boolean checksumMatches(int checksum, byte[] buffer) {
		if (buffer == null || buffer.length < 2) {
			return false;
		}
		
		int length = buffer.length - 2;
		CRC32 crc32 = new CRC32();
		crc32.reset();
		crc32.update(buffer, 0, length);
		int ourCrc = (int) crc32.getValue();
		return ourCrc == checksum;
	}
	
	public FileRequest requestFile(int index, int file, boolean needsComplete) {
		int filePair = FilePair.create(index, file);
		FileRequest madeRequest = getFileRequest(filePair);
		if (madeRequest != null) return madeRequest;
		
		requestIndexedChecksums();
		
		FileRequest request = new FileRequest(filePair, needsComplete);
		synchronized (fileRequests) {
			fileRequests.put(filePair, request);
		}
		
		if (index > 0) {
			final FileStore store = getFileStore(index);
			if (store != null) {
				final boolean containsKey;
				synchronized (checksums) {
					containsKey = checksums.containsKey(filePair);
				}
				if (containsKey) {
					final byte[] data = store.read(file);
					if (data != null) {
						final int checksum;
						synchronized (checksums) {
							checksum = checksums.get(filePair);
						}
						if (checksumMatches(checksum, data)) {
							request.complete(fileRequestResponseSupplier.supply(filePair, data));
							return request;
						}
					}
				}
			}
		}
		
		channel.write(request, channel.voidPromise());
		return request;
	}
	
	public boolean isArchiveUpdated(int index, int file, int checksum) {
		int pair = FilePair.create(index, file);
		synchronized (checksums) {
			return checksums.containsKey(pair) && checksums.get(pair) == checksum;
		}
	}
	
	public boolean completeChecksumsRequest(FileChecksumsResponse fileChecksumsResponse) {
		return fileChecksumsRequest.complete(fileChecksumsResponse);
	}
	
	public FileRequestResponseSupplier getFileRequestResponseSupplier() {
		return fileRequestResponseSupplier;
	}
	
}
