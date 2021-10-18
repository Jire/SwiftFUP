package org.jire.swiftfp.client.crc32;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.jire.swiftfp.client.*;

import java.util.zip.CRC32;

/**
 * @author Jire
 */
public abstract class CRC32FileRequester
		<RESPONSE extends CRC32FileResponse>
		extends AbstractFileRequester<RESPONSE>
		implements FileChecksumRequester, CRC32FileResponder<RESPONSE> {
	
	private final Int2IntMap checksums;
	
	private FileChecksumsRequest fileChecksumsRequest;
	
	public CRC32FileRequester(int expected) {
		super(expected);
		
		checksums = new Int2IntOpenHashMap(expected);
	}
	
	public abstract FileStore getFileStore(int index);
	
	@Override
	public FileRequest<RESPONSE> newRequest(int filePair) {
		FileRequest<RESPONSE> request = super.newRequest(filePair);
		final int index = FilePair.index(filePair);
		if (index > 0) {
			final FileStore store = getFileStore(index);
			if (store != null) {
				final byte[] data = store.read(FilePair.file(filePair));
				if (data != null) {
					final int checksum = getChecksum(filePair);
					if (checksumMatches(checksum, data)) {
						request.complete(newResponse(filePair, data));
						return request;
					}
				}
			}
		}
		return request;
	}
	
	@Override
	public void setChecksum(int filePair, int checksum) {
		checksums.put(filePair, checksum);
	}
	
	@Override
	public int getChecksum(int filePair) {
		return checksums.get(filePair);
	}
	
	@Override
	public FileChecksumsRequest requestChecksums() {
		return fileChecksumsRequest == null
				? fileChecksumsRequest = new FileChecksumsRequest()
				: fileChecksumsRequest;
	}
	
	@Override
	public boolean completeChecksumsRequest(FileChecksumsResponse checksumsResponse) {
		return fileChecksumsRequest.complete(checksumsResponse);
	}
	
	@Override
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
	
	@Override
	public boolean checksumMatches(int filePair, int checksum) {
		return checksums.containsKey(filePair)
				&& checksums.get(filePair) == checksum;
	}
	
}
