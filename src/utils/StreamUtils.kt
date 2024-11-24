package utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream

fun BufferedOutputStream.writeVariableLength(frequency: Long) {
    val encodedFrequency = encodeVariableLengthLong(frequency)
    encodedFrequency.forEach { element ->
        write(element.toInt())
    }
}

fun BufferedOutputStream.writeString(str: String) {
    str.forEach { char ->
        write(char.code)
    }
    write(0)
}

fun BufferedInputStream.readString(buffer: CharArray = CharArray(64)): String {
    var position = 0

    while (true) {
        val byte = read()
        if (byte == 0 || byte == -1) break

        if (position >= buffer.size) {
            error("buffer of insufficient length")
        }

        buffer[position++] = byte.toChar()
    }

    return String(buffer, 0, position)
}