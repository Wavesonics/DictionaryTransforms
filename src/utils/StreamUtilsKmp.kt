package utils

import okio.BufferedSink
import okio.BufferedSource

fun BufferedSink.writeString(str: String) {
    write(str.encodeToByteArray())
    writeByte(0) // null terminator
}

fun BufferedSource.readString(): String {
    val bytes = mutableListOf<Byte>()
    while (true) {
        if (exhausted()) break
        val byte = readByte()
        if (byte == 0.toByte()) break
        bytes.add(byte)
    }
    return bytes.toByteArray().decodeToString()
}