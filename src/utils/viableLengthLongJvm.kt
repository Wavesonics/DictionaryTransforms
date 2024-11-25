package utils

import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import kotlin.experimental.or

fun encodeVariableLengthLong(value: Long, buffer: ByteArray = ByteArray(10)): ByteArray {
    var remainingValue = value
    var n = 0
    do {
        var byte = (remainingValue and 0x7F).toByte() // Take the lowest 7 bits
        remainingValue = remainingValue ushr 7       // Shift the value to the right by 7 bits
        if (remainingValue != 0L) {
            byte = byte or 0x80.toByte()            // Set MSB to indicate continuation
        }
        buffer[n] = byte
        n++
    } while (remainingValue != 0L)
    return buffer.copyOfRange(0, n)
}

fun encodeVariableLengthLong(value: Long, outputStream: OutputStream) {
    var remainingValue = value
    do {
        var byte = (remainingValue and 0x7F).toByte() // Take the lowest 7 bits
        remainingValue = remainingValue ushr 7       // Shift the value to the right by 7 bits
        if (remainingValue != 0L) {
            byte = byte or 0x80.toByte()            // Set MSB to indicate continuation
        }
        outputStream.write(byte.toInt())
    } while (remainingValue != 0L)
}

fun decodeVariableLengthLong(input: InputStream): Long {
    var value = 0L
    var shift = 0
    var n = 0
    while (true) {
        ++n
        val byte = input.read()
        val data = ((byte.toLong() and 0x7F) shl shift) // Extract the 7 bits and shift them
        if (byte == -1) throw EOFException("Unexpected end of file while reading variable-length value")
        value = value or data
        if (byte and 0x80 == 0) { // MSB is 0, end of number
            break
        }
        shift += 7
    }
    return value
}
