package org.jire.swiftfup.server.net

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.WriteBufferWaterMark
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.NettyRuntime
import io.netty.util.ResourceLeakDetector
import kotlin.math.max

/**
 * @author Jire
 */
class FileServer(
    private val fileRequestResponses: FileRequestResponses,

    private val parentGroup: EventLoopGroup =
        eventLoopGroup(1),
    private val childGroup: EventLoopGroup =
        eventLoopGroup(max(1, NettyRuntime.availableProcessors() - 1)),

    private val channelClass: Class<out ServerChannel> = serverChannelClass(parentGroup)
) {

    private fun createBootstrap(): ServerBootstrap = ServerBootstrap()
        .group(parentGroup, childGroup)
        .channel(channelClass)

        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120_000)

        .childOption(ChannelOption.SO_SNDBUF, 65536)
        .childOption(ChannelOption.SO_RCVBUF, 65536)

        .childOption(
            ChannelOption.WRITE_BUFFER_WATER_MARK,
            WriteBufferWaterMark(8192, 131072)
        )

        .childHandler(FileServerChannelInitializer(fileRequestResponses))

    fun start(vararg ports: Int, print: Boolean = true) = createBootstrap().run {
        if (print) println("[Binding to ports]")
        for (port in ports) {
            if (print) print("    port $port [...]")

            bind(port)
                .syncUninterruptibly()
                .get()

            if (print) println("\b\b\b\bdone]")
        }
    }

    init {
        // you can set this to PARANOID for development
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED)
    }

    private companion object {

        fun eventLoopGroup(numThreads: Int = 0) = when {
            Epoll.isAvailable() -> EpollEventLoopGroup(numThreads)
            KQueue.isAvailable() -> KQueueEventLoopGroup(numThreads)
            else -> NioEventLoopGroup(numThreads)
        }

        fun serverChannelClass(group: EventLoopGroup): Class<out ServerChannel> = when (group) {
            is EpollEventLoopGroup -> EpollServerSocketChannel::class.java
            is KQueueEventLoopGroup -> KQueueServerSocketChannel::class.java
            else -> NioServerSocketChannel::class.java
        }

    }

}
