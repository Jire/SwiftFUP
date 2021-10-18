package org.jire.swiftfup.client.net.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.jire.swiftfup.client.crc32.CRC32FileResponder;

import java.util.List;

/**
 * @author Jire
 */
public final class FileResponseDecoder extends ByteToMessageDecoder {
	
	private final CRC32FileResponder<?> fileResponder;
	
	public FileResponseDecoder(CRC32FileResponder<?> fileResponder) {
		this.fileResponder = fileResponder;
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
			
			fileResponder.complete(filePair, data, dataSize);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
}
