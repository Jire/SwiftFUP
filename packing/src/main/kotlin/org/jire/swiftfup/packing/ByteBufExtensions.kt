package org.jire.swiftfup.packing

import io.netty.buffer.ByteBuf
import io.netty.util.ByteProcessor
import java.nio.charset.Charset

private const val STRING_VERSION = 0

fun ByteBuf.readShortSmart(): Int {
    val peek = getUnsignedByte(readerIndex()).toInt()
    return if ((peek and 0x80) == 0) {
        readUnsignedByte().toInt() - 0x40
    } else {
        (readUnsignedShort() and 0x7FFF) - 0x4000
    }
}

fun ByteBuf.writeShortSmart(v: Int): ByteBuf {
    when (v) {
        in -0x40..0x3F -> writeByte(v + 0x40)
        in -0x4000..0x3FFF -> writeShort(0x8000 or (v + 0x4000))
        else -> throw IllegalArgumentException()
    }

    return this
}

fun ByteBuf.readUnsignedShortSmart(): Int {
    val peek = getByte(readerIndex()).toInt() and 0xFF
    return if (peek < 128) {
        readUnsignedByte().toInt()
    } else {
        readUnsignedShort() - 0x8000
    }
}

fun ByteBuf.writeUnsignedShortSmart(i: Int): ByteBuf {
    if (i < 0 || i > Short.MAX_VALUE) {
        throw RuntimeException("A smart can one be within the boundaries of a signed short.")
    }
    if (i >= 0x80) {
        writeShort(i + 0x8000)
    } else {
        writeByte(i)
    }

    return this
}

fun ByteBuf.readIntSmart(): Int {
    val peek = getUnsignedByte(readerIndex()).toInt()
    return if ((peek and 0x80) == 0) {
        readUnsignedShort() - 0x4000
    } else {
        (readInt() and 0x7FFFFFFF) - 0x40000000
    }
}

fun ByteBuf.writeIntSmart(v: Int): ByteBuf {
    when (v) {
        in -0x4000..0x3FFF -> writeShort(v + 0x4000)
        in -0x40000000..0x3FFFFFFF -> writeInt(0x80000000.toInt() or (v + 0x40000000))
        else -> throw IllegalArgumentException()
    }

    return this
}

fun ByteBuf.readUnsignedIntSmart(): Int {
    val peek = getUnsignedByte(readerIndex()).toInt()
    return if ((peek and 0x80) == 0) {
        readUnsignedShort()
    } else {
        readInt() and 0x7FFFFFFF
    }
}

fun ByteBuf.writeUnsignedIntSmart(v: Int): ByteBuf {
    when (v) {
        in 0..0x7FFF -> writeShort(v)
        in 0..0x7FFFFFFF -> writeInt(0x80000000.toInt() or v)
        else -> throw IllegalArgumentException()
    }

    return this
}

@JvmOverloads
fun ByteBuf.readString(charset: Charset = Cp1252Charset): String {
    val start = readerIndex()

    val end = forEachByte(ByteProcessor.FIND_NUL)
    require(end != -1) {
        "Unterminated string"
    }

    val s = toString(start, end - start, charset)
    readerIndex(end + 1)
    return s
}

@JvmOverloads
fun ByteBuf.writeString(s: CharSequence, charset: Charset = Cp1252Charset): ByteBuf {
    writeCharSequence(s, charset)
    writeByte(0)
    return this
}

fun ByteBuf.readVersionedString(): String {
    val version = readUnsignedByte().toInt()
    require(version == STRING_VERSION) {
        "Unsupported version number $version"
    }
    return readString()
}

fun ByteBuf.writeVersionedString(s: CharSequence): ByteBuf {
    writeByte(STRING_VERSION)
    writeString(s)
    return this
}

fun ByteBuf.readVarInt(): Int {
    var value = 0

    var byte: Int
    do {
        byte = readUnsignedByte().toInt()
        value = (value shl 7) or (byte and 0x7F)
    } while ((byte and 0x80) != 0)

    return value
}

fun ByteBuf.writeVarInt(v: Int): ByteBuf {
    if ((v and 0x7F.inv()) != 0) {
        if ((v and 0x3FFF.inv()) != 0) {
            if ((v and 0x1FFFFF.inv()) != 0) {
                if ((v and 0xFFFFFFF.inv()) != 0) {
                    writeByte(((v ushr 28) and 0x7F) or 0x80)
                }

                writeByte(((v ushr 21) and 0x7F) or 0x80)
            }

            writeByte(((v ushr 14) and 0x7F) or 0x80)
        }

        writeByte(((v ushr 7) and 0x7F) or 0x80)
    }

    writeByte(v and 0x7F)

    return this
}