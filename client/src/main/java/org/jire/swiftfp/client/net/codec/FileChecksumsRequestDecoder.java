package org.jire.swiftfp.client.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.jire.swiftfp.client.FileChecksumsResponse;
import org.jire.swiftfp.client.FileClient;

import java.util.List;

/**
 * @author Jire
 */
public final class FileChecksumsRequestDecoder extends ByteToMessageDecoder {
	
	private final FileClient fileClient;
	
	public FileChecksumsRequestDecoder(FileClient fileClient) {
		this.fileClient = fileClient;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		in.markReaderIndex();
		
		int checksumsCount = in.readUnsignedByte();
		if (!in.isReadable(checksumsCount * 7)) {
			in.resetReaderIndex();
			return;
		}
		
		FileChecksumsResponse.Builder builder = new FileChecksumsResponse.Builder(checksumsCount);
		for (int i = 0; i < checksumsCount; i++) {
			int pair = in.readMedium();
			int crc32 = in.readInt();
			builder.add(pair, crc32);
		}
		fileClient.completeChecksumsRequest(builder.build());
		
		ctx.pipeline().remove(this)
				.addLast(FileRequestEncoder.INSTANCE)
				.addLast(new FileRequestDecoder(fileClient));
		
		if (in.isReadable()) {
			ByteBuf rest = in.readBytes(in.readableBytes());
			out.add(rest);
		}
	}
	
}
