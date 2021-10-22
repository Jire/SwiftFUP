package org.jire.swiftfup.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.jire.swiftfup.client.FileRequests;
import org.jire.swiftfup.client.FileResponse;

import java.util.List;

/**
 * @author Jire
 */
public final class FileResponseDecoder extends ByteToMessageDecoder {
	
	private final FileRequests fileRequests;
	
	public FileResponseDecoder(FileRequests fileRequests) {
		this.fileRequests = fileRequests;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		while (in.isReadable(6)) {
			in.markReaderIndex();
			
			int filePair = in.readMedium();
			
			int dataSize = in.readUnsignedMedium();
			if (!in.isReadable(dataSize)) {
				in.resetReaderIndex();
				return;
			}
			
			final byte[] data = dataSize > 0 ? new byte[dataSize] : null;
			if (data != null) in.readBytes(data);
			
			fileRequests.notify(new FileResponse(filePair, data));
		}
	}
	
}
