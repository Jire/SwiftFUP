package org.jire.swiftfup.packing.roatz

import com.googlecode.pngtastic.core.PngImage
import com.googlecode.pngtastic.core.PngOptimizer
import com.tinify.Tinify
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import javax.imageio.ImageIO

object RoatzImageOptimizer {

    private val logger: Logger = LoggerFactory.getLogger(RoatzImageOptimizer::class.java)

    fun PngImage.writeImage(): BufferedImage {
        val compressedPngData = ByteArrayOutputStream()
        val outputStreamWrapper = DataOutputStream(compressedPngData)
        outputStreamWrapper.writeLong(PngImage.SIGNATURE)
        for (chunk in chunks) {
            outputStreamWrapper.writeInt(chunk.length)
            outputStreamWrapper.write(chunk.type)
            outputStreamWrapper.write(chunk.data)
            val i = chunk.getCRC().toInt()
            outputStreamWrapper.writeInt(i)
        }
        outputStreamWrapper.close()
        compressedPngData.close()

        val data: ByteArray = compressedPngData.toByteArray()
        return ImageIO.read(ByteArrayInputStream(data))
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val optimizer = PngOptimizer()

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
                    val sourceImg = PngImage(source)
                    val resultImg = optimizer.optimize(sourceImg)//Tinify.fromBuffer(source).toBuffer()
                    val image = resultImg.writeImage()
                    ImageIO.write(image, file.extension.lowercase(), file)

                    val sourceSize = source.size
                    val resultSize = file.readBytes().size

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

                    //file.writeBytes(result)

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