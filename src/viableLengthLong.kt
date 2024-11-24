import java.io.EOFException
import java.io.InputStream
import kotlin.experimental.or

fun encodeVariableLength(value: Long): ByteArray {
    val buffer = mutableListOf<Byte>()
    var remainingValue = value
    var n = 0
    do {
        n++
        var byte = (remainingValue and 0x7F).toByte() // Take the lowest 7 bits
        remainingValue = remainingValue ushr 7       // Shift the value to the right by 7 bits
        if (remainingValue != 0L) {
            byte = byte or 0x80.toByte()            // Set MSB to indicate continuation
        }
        buffer.add(byte)
    } while (remainingValue != 0L)
    return buffer.toByteArray()
}

fun decodeVariableLength(input: InputStream): Long {
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


//fun decodeVariableLength(input: ByteArray): Long {
//    var value = 0L
//    var shift = 0
//    for (byte in input) {
//        value = value or ((byte.toLong() and 0x7F) shl shift) // Extract the 7 bits and shift them
//        if (byte.toInt() and 0x80 == 0) {                    // MSB is 0, end of number
//            break
//        }
//        shift += 7
//    }
//    return value
//}
