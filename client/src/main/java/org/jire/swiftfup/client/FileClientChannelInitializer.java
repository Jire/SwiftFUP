package org.jire.swiftfup.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.jire.swiftfup.client.codec.FileChecksumsRequestEncoder;
import org.jire.swiftfup.client.codec.FileChecksumsResponseDecoder;
import org.jire.swiftfup.client.codec.FileRequestEncoder;
import org.jire.swiftfup.client.codec.FileResponseDecoder;

/**
 * @author Jire
 */
public final class FileClientChannelInitializer extends ChannelInitializer<Channel> {
	
	private final FileRequests fileRequests;
	private final boolean reconnect;
	
	public FileClientChannelInitializer(FileRequests fileRequests, boolean reconnect) {
		this.fileRequests = fileRequests;
		this.reconnect = reconnect;
	}
	
	@Override
	protected void initChannel(Channel ch) {
		ch.pipeline().addLast(
				reconnect ? FileRequestEncoder.INSTANCE : FileChecksumsRequestEncoder.INSTANCE,
				reconnect ? new FileResponseDecoder(fileRequests) : new FileChecksumsResponseDecoder(fileRequests)
		);
	}
	
}
