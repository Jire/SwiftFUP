package org.jire.swiftfup.server.net

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled.directBuffer
import org.jire.swiftfup.server.Main

/**
 * @author Jire
 */
enum class HandshakeResponse(val buf: ByteBuf) {

    SUCCESS(
        directBuffer(4, 4)
            .writeByte(0)
            .writeMedium(Main.VERSION)
            .asReadOnly()
    ),

    VERSION_MISMATCH(
        directBuffer(1, 1)
            .writeByte(1)
            .asReadOnly()
    )

    ;

}