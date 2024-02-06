package org.jire.swiftfup.server

import org.jire.swiftfup.server.net.FileResponses
import org.jire.swiftfup.server.net.FileServer

/**
 * @author Jire
 */
object Main {

    const val VERSION = 3

    private const val DEFAULT_CACHE_PATH = "cache"
    private val defaultPorts = intArrayOf(43595)

    @JvmStatic
    fun main(args: Array<String>) {
        /* determine arguments */
        val cachePath = if (args.isEmpty()) DEFAULT_CACHE_PATH else args[0]
        val ports = if (args.size <= 1) defaultPorts else args.drop(1).map { it.toInt() }.toIntArray()
        val print = true

        /* load cached responses */
        val fileResponses = FileResponses()
        fileResponses.load(cachePath, print)

        /* start server */
        FileServer(VERSION, fileResponses).start(*ports, print = print)
    }

}