package org.jire.swiftfp.client.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.jire.swiftfp.client.FileClient;
import org.jire.swiftfp.client.FilePair;
import org.jire.swiftfp.client.FileRequestResponse;

import java.util.List;

/**
 * @author Jire
 */
public final class FileRequestDecoder extends ByteToMessageDecoder {
	
	private final FileClient fileClient;
	
	public FileRequestDecoder(FileClient fileClient) {
		this.fileClient = fileClient;
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
			
			final FileRequestResponse response = fileClient.getFileRequestResponseSupplier().supply(filePair, data);
			
			fileClient.completeFileRequest(response);
			if (FilePair.index(filePair) > 0) fileClient.markLoadedData(response);
			fileClient.writeToStore(response);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
}
