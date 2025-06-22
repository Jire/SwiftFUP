package org.jire.swiftfup.server.net

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.WriteBufferWaterMark
import io.netty.util.ResourceLeakDetector

/**
 * @author Jire
 */
class FileServer(
    private val version: Int,
    private val fileResponses: FileResponses,

    private val bootstrapFactory: BootstrapFactory = BootstrapFactory(),

    private val parentGroup: EventLoopGroup =
        bootstrapFactory.createParentLoopGroup(),
    private val childGroup: EventLoopGroup =
        bootstrapFactory.createChildLoopGroup(),
) {

    private fun createBootstrap(): ServerBootstrap =
        bootstrapFactory
            .createServerBootstrap(parentGroup, childGroup)
            .childOption(ChannelOption.AUTO_READ, true)
            .childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 120_000)
            .childOption(
                ChannelOption.WRITE_BUFFER_WATER_MARK,
                WriteBufferWaterMark(2 shl 20, 2 shl 24)
            )
            .childHandler(FileServerChannelInitializer(version, fileResponses))

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

}
