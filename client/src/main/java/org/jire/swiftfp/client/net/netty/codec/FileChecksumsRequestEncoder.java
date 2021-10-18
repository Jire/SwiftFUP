package org.jire.swiftfp.client.net.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jire.swiftfp.client.FileChecksumsRequest;

/**
 * @author Jire
 */
@ChannelHandler.Sharable
public final class FileChecksumsRequestEncoder extends MessageToByteEncoder<FileChecksumsRequest> {
	
	public static final FileChecksumsRequestEncoder INSTANCE = new FileChecksumsRequestEncoder();
	
	@Override
	protected void encode(ChannelHandlerContext ctx, FileChecksumsRequest msg, ByteBuf out) {
		out.writeByte(0);
		
		ctx.pipeline().remove(this);
	}
	
}
