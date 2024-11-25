package utils

import kotlinx.io.Sink
import kotlinx.io.Source
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import kotlin.experimental.or

fun encodeVariableLengthLong(value: Long, sink: Sink) {
    var remainingValue = value
    do {
        var byte = (remainingValue and 0x7F).toByte()
        remainingValue = remainingValue ushr 7
        if (remainingValue != 0L) {
            byte = byte or 0x80.toByte()
        }
        sink.writeByte(byte)
    } while (remainingValue != 0L)
}

fun Source.decodeVariableLengthLong(): Long {
    var value = 0L
    var shift = 0
    var n = 0
    while (true) {
        ++n
        val byte = readByte().toInt()
        val data = ((byte.toLong() and 0x7F) shl shift)
        value = value or data
        if (byte and 0x80 == 0) {
            break
        }
        shift += 7
    }
    return value
}