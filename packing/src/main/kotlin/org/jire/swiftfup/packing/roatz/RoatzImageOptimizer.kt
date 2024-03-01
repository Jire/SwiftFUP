package org.jire.swiftfup.packing.roatz

import com.tinify.Tinify
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object RoatzImageOptimizer {

    private val logger: Logger = LoggerFactory.getLogger(RoatzImageOptimizer::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val tinifyApiKey = System.getenv("TINIFY_API_KEY")
        Tinify.setKey(tinifyApiKey)

        logger.info("Tinify API key \"{}\"", tinifyApiKey)

        var totalOptimized = 0

        var totalSourceSize = 0
        var totalResultSize = 0

        val dirPath = "data/roatz/cache/Sprites/"
        File(dirPath)
            .walkTopDown()
            .forEach { file ->
                try {
                    if (!file.isFile) return@forEach

                    val fileName = file.name

                    if (".DS_Store" == fileName) {
                        file.delete()
                        return@forEach
                    }

                    val source = file.readBytes()
                    val result = Tinify.fromBuffer(source).toBuffer()

                    val sourceSize = source.size
                    val resultSize = result.size

                    totalSourceSize += sourceSize
                    totalResultSize += resultSize

                    if (resultSize >= sourceSize) {
                        logger.warn(
                            "Result ({} bytes) of \"{}\" <= source ({} bytes)! Skipping...",
                            resultSize,
                            fileName,
                            sourceSize
                        )
                        return@forEach
                    }

                    file.writeBytes(result)

                    logger.info(
                        "Optimized \"{}\" (shrank {}%)",
                        fileName,
                        ((resultSize.toDouble() / sourceSize.toDouble()) * 100).toInt()
                    )

                    totalOptimized++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        logger.info(
            "Finished optimizing {} images! (shrank {}% from {} bytes down to {} bytes)",
            totalOptimized,
            ((totalResultSize.toDouble() / totalSourceSize.toDouble()) * 100).toInt(),
            totalSourceSize,
            totalResultSize
        )
    }

}