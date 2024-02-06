package org.jire.swiftfup.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
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

    private final Int2ObjectMap<FileRequest> requests;

    private final MessagePassingQueue<FileRequest> pendingRequests;

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

        requests = new Int2ObjectOpenHashMap<>(capacity);

        pendingRequests = new MpscArrayQueue<>(capacity);

        completedResponses = new MpscArrayQueue<>(capacity);

        decompressedResponses = new MpscArrayQueue<>(capacity);
        this.fileDecompressedListener = fileDecompressedListener;
    }

    public FileRequest filePair(final int filePair) {
        FileRequest request;
        synchronized (requests) {
            request = requests.get(filePair);
        }

        if (request == null || filePair == FilePair.CHECKSUMS_FILE_PAIR) {
            request = new FileRequest(filePair);

            synchronized (requests) {
                requests.put(filePair, request);
            }
            pendingRequests.offer(request);
        } else if (FilePair.index(filePair) > 0) {
            // this code should ONLY be called on the game client thread

            FileResponse response = request.getNow(null);
            if (response != null) {
                byte[] decompressedData = response.getDecompressedData();
                if (decompressedData != null) {
                    fileDecompressedListener.decompressed(response);
                }
            }
        }

        return request;
    }

    public FileRequest file(final int index, final int file) {
        return filePair(FilePair.create(index, file));
    }

    public FileRequest checksums() {
        return filePair(FilePair.CHECKSUMS_FILE_PAIR);
    }

    public void process(FileClient fileClient) {
        processCompletedResponses();

        // last because we still want to process any completed requests from earlier
        // before potentially waiting for channel to reconnect
        processPendingRequests(fileClient);
    }

    public void processPendingRequests(FileClient fileClient) {
        final Channel channel = fileClient.getConnectedChannel();
        if (channel == null) return;

        boolean flush = false;

        final MessagePassingQueue<FileRequest> pendingRequests = this.pendingRequests;
        while (channel.isActive() && !pendingRequests.isEmpty()) {
            final FileRequest request = pendingRequests.poll();
            if (request == null) break;

            // make sure the request wasn't already completed, so that we ignore duplicate requests
            if (request.isDone()) continue;

            final int filePair = request.getFilePair();

            if (filePair != FilePair.CHECKSUMS_FILE_PAIR) {
                final int index = FilePair.index(filePair);
                final int file = FilePair.file(filePair);

                if (index > 0) {
                    byte[] diskData = getDiskData(filePair);
                    if (checksumMatches(filePair, diskData)) {
                        FileResponse response = new FileResponse(filePair, diskData);

                        notifyDecompressed(response);

                        request.complete(response);
                        continue;
                    }
                }

                request.thenAcceptAsync(response -> {
                    byte[] data = response.getData();
                    if (data != null && data.length > 0) {
                        FileIndex fileIndex = fileStore.getIndex(index);
                        fileIndex.writeFile(file, data);
                    }
                });
            }

            // XXX: what do we do if channel disconnected in the middle of writing?
            // in that case, the request won't have been written to the right channel!
            channel.write(request, channel.voidPromise());
            flush = true;
        }

        if (flush && channel.isActive()) {
            channel.flush();
        }
    }

    private void processCompletedResponses() {
        final MessagePassingQueue<FileResponse> completedResponses = this.completedResponses;
        final Int2ObjectMap<FileRequest> requests = this.requests;
        while (!completedResponses.isEmpty()) {
            final FileResponse response = completedResponses.poll();
            if (response == null) break;

            final int filePair = response.getFilePair();

            FileRequest request;
            synchronized (requests) {
                request = requests.get(filePair);
            }
            request.complete(response);

            if (filePair == FilePair.CHECKSUMS_FILE_PAIR) {
                notifyChecksums(response);
            } else if (FilePair.index(filePair) > 0) {
                notifyDecompressed(response);
            }
        }
    }

    public void processDecompressedResponses() {
        final MessagePassingQueue<FileResponse> decompressedResponses = this.decompressedResponses;
        final FileDecompressedListener fileDecompressedListener = this.fileDecompressedListener;
        while (!decompressedResponses.isEmpty()) {
            final FileResponse response = decompressedResponses.poll();
            if (response == null) break;

            fileDecompressedListener.decompressed(response);
        }
    }

    public void notifyChecksums(FileResponse response) {
        final Int2IntMap checksums = this.checksums;
        // the reason we synchronize for the whole block rather than just checksums.put
        // is because we want it to stay locked from modification from other file requests in the meanwhile
        synchronized (checksums) {
            final byte[] compressedBytes = response.getData();
            final byte[] decompressedBytes = GZIPDecompressor.getInstance().decompress(compressedBytes);

            final ByteBuf data = Unpooled.wrappedBuffer(decompressedBytes);
            try {
                final int indexCount = data.readUnsignedByte();
                for (int indexId = 0; indexId < indexCount; indexId++) {

                    final int archiveCount = data.readUnsignedMedium();
                    for (int archiveId = 0; archiveId < archiveCount; archiveId++) {

                        final int crc32 = data.readInt();
                        if (crc32 == 0) continue;

                        final int filePair = FilePair.create(indexId, archiveId);

                        checksums.put(filePair, crc32);
                    }
                }
            } finally {
                data.release();
            }
        }
    }

    public void notify(FileResponse response) {
        completedResponses.offer(response);
    }

    public void notifyDecompressed(FileResponse response) {
        if (response.setDecompressedData(fileStore) != null) {
            decompressedResponses.offer(response);
        }
    }

    public boolean checksumMatches(int filePair, int checksum) {
        return getChecksum(filePair) == checksum;
    }

    public int getChecksum(int filePair) {
        final Int2IntMap checksums = this.checksums;
        synchronized (checksums) {
            return checksums.get(filePair);
        }
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
