package org.jire.swiftfup.server.net

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import org.jire.swiftfup.server.net.codec.FileServerRequestDecoder
import org.jire.swiftfup.server.net.codec.FileServerRequestHandler

/**
 * @author Jire
 */
class FileServerChannelInitializer(
    private val fileRequestResponses: FileRequestResponses
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline()
            .addLast(FileServerRequestDecoder())
            .addLast(FileServerRequestHandler(fileRequestResponses))
    }

}
