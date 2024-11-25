package utils

import kotlinx.io.Sink
import kotlinx.io.Source
import okio.GzipSource

fun Sink.writeVariableLength(frequency: Long) = encodeVariableLengthLong(frequency, this)

fun Sink.writeString(str: String) {
    str.forEach { char ->
        writeByte(char.code.toByte())
    }
    writeByte(0)
}

fun Source.readString(buffer: CharArray = CharArray(64)): String {
    var position = 0

    while (true) {
        val byte = readByte().toInt()
        if (byte == 0 || byte == -1) break

        if (position >= buffer.size) {
            error("buffer of insufficient length")
        }

        buffer[position++] = byte.toChar()
    }

    return String(buffer, 0, position)
}
