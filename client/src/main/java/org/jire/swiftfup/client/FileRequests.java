package org.jire.swiftfup.client;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.zip.CRC32;

/**
 * @author Jire
 */
public final class FileRequests {
	
	private final int capacity;
	private final FileStore fileStore;
	
	private final Int2IntMap checksums;
	private final FileChecksumsRequest checksumsRequest = new FileChecksumsRequest();
	
	private final Int2ObjectMap<FileRequest> requests;
	
	private final MessagePassingQueue<FileResponse> completedResponses;

	private final MessagePassingQueue<FileResponse> decompressedResponses;

	private final FileDecompressedListener fileDecompressedListener;

	private volatile boolean ignoreChecksums;
	
	public FileRequests(int capacity,
	                    FileStore fileStore,
	                    FileDecompressedListener fileDecompressedListener) {
		this.capacity = capacity;
		this.fileStore = fileStore;
		
		checksums = new Int2IntOpenHashMap(capacity);
		checksumsRequest.thenAccept(response -> checksums.putAll(response.getFileToChecksum()));
		
		requests = new Int2ObjectOpenHashMap<>(capacity);
		
		completedResponses = new MpscArrayQueue<>(capacity);

		decompressedResponses = new MpscArrayQueue<>(capacity);
		this.fileDecompressedListener = fileDecompressedListener;
	}
	
	public FileChecksumsRequest checksums(FileClient fileClient) {
		if (!checksumsRequest.isDone())
			fileClient.request(checksumsRequest);
		return checksumsRequest;
	}
	
	public FileRequest filePair(final int filePair, FileClient fileClient) {
		final int index = FilePair.index(filePair);
		final int file = FilePair.file(filePair);
		
		FileRequest request = requests.get(filePair);
		if (request != null && request.isSent()) {
			if (index > 0) {
				FileResponse response = request.getNow(null);
				if (response != null && response.getDecompressedData() != null)
					decompressedResponses.offer(response);
			}
			return request;
		}
		
		request = new FileRequest(filePair);
		requests.put(filePair, request);
		if (index > 0) {
			byte[] diskData = getDiskData(filePair);
			if (checksumMatches(filePair, diskData)) {
				FileResponse response = new FileResponse(filePair, diskData);
				
				/* we must decompress here to have stuff like anims loading within same client frame */
				response.setDecompressedData(fileStore);
				fileDecompressedListener.decompressed(response);

				request.complete(response);
				return request;
			}
			
			request.thenAcceptAsync(this::notifyDecompressed);
		}
		
		request.thenAcceptAsync(response -> {
			byte[] data = response.getData();
			if (data != null && data.length > 0) {
				FileIndex fileIndex = fileStore.getIndex(index);
				fileIndex.writeFile(file, data);
			}
		});
		
		fileClient.request(request);
		return request;
	}
	
	public void process() {
		processCompletedResponses();
		processDecompressedResponses();
	}

	private void processCompletedResponses() {
		final MessagePassingQueue<FileResponse> completedResponses = this.completedResponses;
		final Int2ObjectMap<FileRequest> requests = this.requests;
		while (!completedResponses.isEmpty()) {
			final FileResponse response = completedResponses.poll();

			int filePair = response.getFilePair();
			FileRequest request = requests.get(filePair);
			request.complete(response);
		}
	}

	private void processDecompressedResponses() {
		final MessagePassingQueue<FileResponse> decompressedResponses = this.decompressedResponses;
		final FileDecompressedListener fileDecompressedListener = this.fileDecompressedListener;
		while (!decompressedResponses.isEmpty()) {
			final FileResponse response = decompressedResponses.poll();
			fileDecompressedListener.decompressed(response);
		}
	}
	
	public void notifyChecksums(FileChecksumsResponse response) {
		checksumsRequest.complete(response);
	}
	
	public void notify(FileResponse response) {
		completedResponses.offer(response);
	}
	
	public void notifyDecompressed(FileResponse response) {
		response.setDecompressedData(fileStore);
		decompressedResponses.offer(response);
	}
	
	public boolean checksumMatches(int filePair, int checksum) {
		return checksums.get(filePair) == checksum;
	}
	
	public int getChecksum(int filePair) {
		return checksums.get(filePair);
	}
	
	public boolean checksum(int filePair, byte[] data) {
		return checksumMatchesData(getChecksum(filePair), data);
	}
	
	public byte[] getDiskData(int filePair) {
		int index = FilePair.index(filePair);
		int file = FilePair.file(filePair);
		FileIndex fileIndex = fileStore.getIndex(index);
		return fileIndex == null ? null : fileIndex.getFile(file);
	}
	
	public boolean isIgnoreChecksums() {
		return ignoreChecksums;
	}
	
	public void setIgnoreChecksums(boolean ignoreChecksums) {
		this.ignoreChecksums = ignoreChecksums;
	}
	
	public boolean checksumMatches(int filePair, byte[] data) {
		return (isIgnoreChecksums() && data != null) || checksumMatchesData(getChecksum(filePair), data);
	}
	
	private static final ThreadLocal<CRC32> threadLocalCRC32 = ThreadLocal.withInitial(CRC32::new);
	
	public static boolean checksumMatchesData(int checksum, byte[] data) {
		int dataChecksum = getChecksum(data);
		return checksum == dataChecksum;
	}
	
	public static int getChecksum(byte[] data, CRC32 crc32) {
		if (data == null) return 0;
		crc32.reset();
		crc32.update(data, 0, data.length);
		return (int) crc32.getValue();
	}
	
	public static int getChecksum(byte[] data) {
		return getChecksum(data, threadLocalCRC32.get());
	}
	
}
