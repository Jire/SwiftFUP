package org.jire.swiftfp.client.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import org.jire.swiftfp.client.FileClient;
import org.jire.swiftfp.client.net.codec.FileChecksumsRequestDecoder;
import org.jire.swiftfp.client.net.codec.FileChecksumsRequestEncoder;

/**
 * @author Jire
 */
@ChannelHandler.Sharable
public final class FileClientChannelInitializer extends ChannelInitializer<Channel> {
	
	private final FileClient fileClient;
	
	public FileClientChannelInitializer(FileClient fileClient) {
		this.fileClient = fileClient;
	}
	
	@Override
	protected void initChannel(Channel ch) {
		ch.pipeline()
				.addLast(FileChecksumsRequestEncoder.INSTANCE)
				.addLast("decoder", new FileChecksumsRequestDecoder(fileClient));
	}
	
}
