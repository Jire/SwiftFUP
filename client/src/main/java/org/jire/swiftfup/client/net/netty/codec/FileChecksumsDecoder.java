package org.jire.swiftfup.client.net.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.jire.swiftfup.client.FileChecksumRequester;
import org.jire.swiftfup.client.FileChecksumsResponse;
import org.jire.swiftfup.client.crc32.CRC32FileResponder;

import java.util.List;

/**
 * @author Jire
 */
public final class FileChecksumsDecoder extends ByteToMessageDecoder {
	
	private final FileChecksumRequester fileChecksumRequester;
	private final CRC32FileResponder<?> fileResponder;
	
	public FileChecksumsDecoder(FileChecksumRequester fileChecksumRequester,
	                            CRC32FileResponder<?> fileResponder) {
		this.fileChecksumRequester = fileChecksumRequester;
		this.fileResponder = fileResponder;
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
		fileChecksumRequester.completeChecksumsRequest(builder.build());
		
		ctx.pipeline().remove(this)
				.addLast(FileRequestEncoder.INSTANCE)
				.addLast(new FileResponseDecoder(fileResponder));
		
		if (in.isReadable()) {
			ByteBuf rest = in.readBytes(in.readableBytes());
			out.add(rest);
		}
	}
	
}
