package org.jire.swiftfup.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.jire.swiftfup.client.FileChecksumsResponse;
import org.jire.swiftfup.client.FileRequests;

import java.util.List;

/**
 * @author Jire
 */
public final class FileChecksumsResponseDecoder extends ByteToMessageDecoder {
	
	private final FileRequests fileRequests;
	
	public FileChecksumsResponseDecoder(FileRequests fileRequests) {
		this.fileRequests = fileRequests;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		in.markReaderIndex();
		
		int size = in.readUnsignedMedium();
		if (!in.isReadable(size * 7)) {
			in.resetReaderIndex();
			return;
		}
		
		ctx.pipeline().remove(this)
				.addLast(FileRequestEncoder.INSTANCE)
				.addLast(new FileResponseDecoder(fileRequests));
		
		Int2IntMap fileToChecksum = new Int2IntOpenHashMap(size);
		for (int i = 0; i < size; i++) {
			int filePair = in.readMedium();
			int crc32 = in.readInt();
			
			fileToChecksum.put(filePair, crc32);
		}
		FileChecksumsResponse response = new FileChecksumsResponse(fileToChecksum);
		fileRequests.getChecksumsRequest().complete(response);
	}
	
}
