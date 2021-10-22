package org.jire.swiftfup.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jire.swiftfup.client.FileRequest;

/**
 * @author Jire
 */
@ChannelHandler.Sharable
public final class FileRequestEncoder extends MessageToByteEncoder<FileRequest> {
	
	@Override
	protected void encode(ChannelHandlerContext ctx, FileRequest msg, ByteBuf out) {
		out.writeByte(1).writeMedium(msg.getFilePair());
	}
	
	private FileRequestEncoder() {
	}
	
	public static final FileRequestEncoder INSTANCE = new FileRequestEncoder();
	
}
