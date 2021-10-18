package org.jire.swiftfup.server

import org.jire.swiftfup.server.net.FileRequestResponses
import org.jire.swiftfup.server.net.FileServer

/**
 * @author Jire
 */
object Main {
	
	private const val DEFAULT_CACHE_PATH = "cache"
	private val defaultPorts = intArrayOf(55555/*43595, 43596*/)
	
	@JvmStatic
	fun main(args: Array<String>) {
		/* determine arguments */
		val cachePath = if (args.isEmpty()) DEFAULT_CACHE_PATH else args[0]
		val ports = if (args.size <= 1) defaultPorts else args.drop(1).map { it.toInt() }.toIntArray()
		val print = true
		
		/* load cached responses */
		val fileRequestResponses = FileRequestResponses()
		fileRequestResponses.load(cachePath, print)
		
		/* start server */
		FileServer(fileRequestResponses).start(*ports, print = print)
	}
	
}