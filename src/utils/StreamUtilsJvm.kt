package utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream

//fun BufferedOutputStream.writeVariableLength(frequency: Long, buffer: ByteArray = ByteArray(10)) {
//    val encodedFrequency = encodeVariableLengthLong(frequency, buffer)
//    encodedFrequency.forEach { element ->
//        write(element.toInt())
//    }
//}

fun BufferedOutputStream.writeVariableLength(frequency: Long) = encodeVariableLengthLong(frequency, this)

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