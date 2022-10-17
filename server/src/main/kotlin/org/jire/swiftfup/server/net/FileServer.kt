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
import io.netty.util.ResourceLeakDetector

/**
 * @author Jire
 */
class FileServer(
    private val fileRequestResponses: FileRequestResponses,

    private val eventLoopGroup: EventLoopGroup = bestEventLoopGroup(),
    private val channelClass: Class<out ServerChannel> = bestChannelClass()
) {

    private fun createBootstrap(): ServerBootstrap = ServerBootstrap()
        .group(eventLoopGroup)
        .channel(channelClass)

        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30_000)

        .childOption(ChannelOption.SO_SNDBUF, 65536)
        .childOption(ChannelOption.SO_RCVBUF, 65536)

        .childOption(
            ChannelOption.WRITE_BUFFER_WATER_MARK,
            WriteBufferWaterMark(8192, 131072)
        )

        .childHandler(FileServerChannelInitializer(eventLoopGroup, fileRequestResponses))

    fun start(vararg ports: Int, print: Boolean = true) = createBootstrap().run {
        if (print) println("[Binding to ports]")
        for (port in ports) {
            if (print) print("    port $port [...]")
            bind(port).syncUninterruptibly().get()
            if (print) println("\b\b\b\bdone]")
        }
    }

    init {
        // you can set this to PARANOID for development
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED)
    }

    private companion object {

        fun bestEventLoopGroup() = when {
            Epoll.isAvailable() -> EpollEventLoopGroup()
            KQueue.isAvailable() -> KQueueEventLoopGroup()
            else -> NioEventLoopGroup()
        }

        fun bestChannelClass(): Class<out ServerChannel> = when {
            Epoll.isAvailable() -> EpollServerSocketChannel::class.java
            KQueue.isAvailable() -> KQueueServerSocketChannel::class.java
            else -> NioServerSocketChannel::class.java
        }

    }

}