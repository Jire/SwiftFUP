package org.jire.swiftfp.server.net

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import org.jire.swiftfp.server.net.codec.FileServerRequestDecoder

/**
 * @author Jire
 */
@Sharable
class FileServerChannelInitializer(
	private val fileRequestResponses: FileRequestResponses
) : ChannelInitializer<SocketChannel>() {
	
	override fun initChannel(ch: SocketChannel) {
		ch.pipeline().addLast(FileServerRequestDecoder(fileRequestResponses))
	}
	
}