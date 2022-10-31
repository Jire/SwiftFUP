package org.jire.swiftfup.server.net

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateHandler
import org.jire.swiftfup.server.net.codec.FileServerRequestDecoder
import org.jire.swiftfup.server.net.codec.FileServerRequestHandler
import java.util.concurrent.TimeUnit

/**
 * @author Jire
 */
class FileServerChannelInitializer(
    private val fileRequestResponses: FileRequestResponses
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline()
            .addLast(IdleStateHandler(true, 0, 0, IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS))
            .addLast(FileServerRequestDecoder())
            .addLast(FileServerRequestHandler(fileRequestResponses))
    }

    private companion object {
        private const val IDLE_TIMEOUT_SECONDS = 120L
    }

}
