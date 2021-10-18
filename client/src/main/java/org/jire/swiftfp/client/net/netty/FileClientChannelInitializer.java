package org.jire.swiftfp.client.net.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import org.jire.swiftfp.client.FileChecksumRequester;
import org.jire.swiftfp.client.crc32.CRC32FileResponder;
import org.jire.swiftfp.client.net.netty.codec.FileChecksumsDecoder;
import org.jire.swiftfp.client.net.netty.codec.FileChecksumsRequestEncoder;

/**
 * @author Jire
 */
@ChannelHandler.Sharable
public final class FileClientChannelInitializer extends ChannelInitializer<Channel> {
	
	private final FileChecksumRequester fileChecksumRequester;
	private final CRC32FileResponder<?> fileResponder;
	
	public FileClientChannelInitializer(FileChecksumRequester fileChecksumRequester,
	                                    CRC32FileResponder<?> fileResponder) {
		this.fileChecksumRequester = fileChecksumRequester;
		this.fileResponder = fileResponder;
	}
	
	@Override
	protected void initChannel(Channel ch) {
		ch.pipeline()
				.addLast(FileChecksumsRequestEncoder.INSTANCE)
				.addLast("decoder", new FileChecksumsDecoder(fileChecksumRequester, fileResponder));
	}
	
}
