package org.jire.swiftfup.client;

import com.leanbow.shoo.go.away.Client;
import com.soulplayps.client.fs.RSFileStore;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;
import io.netty.util.internal.shaded.org.jctools.queues.SpscLinkedQueue;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.zip.CRC32;

/**
 * @author Jire
 */
public class FileRequests {
	
	private final int capacity;
	
	private final Int2IntMap checksums;
	private final FileChecksumsRequest checksumsRequest = new FileChecksumsRequest();
	
	private final Int2ObjectMap<FileRequest> requests;
	
	private final MessagePassingQueue<FileResponse> completedResponses;
	private final MessagePassingQueue.Consumer<FileResponse> completedResponseConsumer;
	
	private final MessagePassingQueue<FileResponse> decompressedResponses;
	private final MessagePassingQueue.Consumer<FileResponse> decompressedResponseConsumer;
	
	private final MessagePassingQueue<FileResponse> incompleteResponses;
	private final MessagePassingQueue.Consumer<FileResponse> incompleteResponseConsumer;
	
	public FileRequests(int capacity) {
		this.capacity = capacity;
		
		checksums = new Int2IntOpenHashMap(capacity);
		checksumsRequest.thenAccept(response -> checksums.putAll(response.getFileToChecksum()));
		
		requests = new Int2ObjectOpenHashMap<>(capacity);
		
		completedResponses = new MpscArrayQueue<>(capacity);
		completedResponseConsumer = response -> {
			int filePair = response.getFilePair();
			FileRequest request = requests.get(filePair);
			request.complete(response);
		};
		
		decompressedResponses = new MpscArrayQueue<>(capacity);
		incompleteResponses = new SpscLinkedQueue<>();
		decompressedResponseConsumer = response -> Client.instance.processOnDemandData(response);
		incompleteResponseConsumer = decompressedResponses::offer;
	}
	
	public FileChecksumsRequest getChecksumsRequest() {
		return checksumsRequest;
	}
	
	public FileChecksumsRequest checksums(FileClient fileClient) {
		if (!checksumsRequest.isDone())
			fileClient.request(checksumsRequest);
		return checksumsRequest;
	}
	
	public FileRequest filePair(int filePair, FileClient fileClient) {
		FileRequest request = requests.get(filePair);
		if (request != null) return request;
		
		int index = FilePair.index(filePair);
		int file = FilePair.file(filePair);
		
		request = new FileRequest(filePair);
		requests.put(filePair, request);
		if (index > 0) {
			request.thenAcceptAsync(response -> {
				decompressedResponses.offer(response);
			});
			
			RSFileStore fileStore = Client.instance.fileStores[index];
			if (fileStore != null) {
				byte[] fileStoreData = fileStore.readFile(file);
				if (fileStoreData != null) {
					int checksum = checksums.get(filePair);
					if (checksumMatches(checksum, fileStoreData)) {
						request.complete(new FileResponse(filePair, fileStoreData));
						System.out.println("CRC GUCCI! " + FilePair.toString(filePair));
						return request;
					}
				}
			}
		}
		
		request.thenAcceptAsync(response -> {
			byte[] data = response.getData();
			Client.instance.fileStores[index].writeFile(data.length, data, file);
		});
		
		fileClient.request(request);
		return request;
	}
	
	public void process() {
		System.out.println("NIGGA " + completedResponses.size());
		completedResponses.drain(completedResponseConsumer, capacity);
		System.out.println("BRO WTF " + requests.values().stream().filter(r -> !r.isDone()).count() + " VS " + decompressedResponses.size() + " VS " + incompleteResponses.size());
		incompleteResponses.drain(incompleteResponseConsumer, capacity);
		decompressedResponses.drain(decompressedResponseConsumer, capacity);
	}
	
	public void receiveResponse(FileResponse fileResponse) {
		completedResponses.offer(fileResponse);
	}
	
	public boolean checksumMatches(int filePair, int checksum) {
		return checksums.get(filePair) == checksum;
	}
	
	public boolean checksumMatches(int checksum, byte[] buffer) {
		if (buffer == null || buffer.length < 2)
			return false;
		
		int length = buffer.length - 2;
		CRC32 crc32 = new CRC32();
		crc32.reset();
		crc32.update(buffer, 0, length);
		int ourCrc = (int) crc32.getValue();
		return ourCrc == checksum;
	}
	
}
