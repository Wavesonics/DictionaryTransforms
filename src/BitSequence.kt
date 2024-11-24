// Represents a sequence of bits more efficiently than a String
class BitSequence {
    private val data: MutableList<Byte> = mutableListOf()
    var length: Int = 0
        private set

    fun append(bit: Boolean) {
        val byteIndex = length / 8
        val bitIndex = 7 - (length % 8)  // MSB first

        if (byteIndex >= data.size) {
            data.add(0)
        }

        if (bit) {
            data[byteIndex] = (data[byteIndex].toInt() or (1 shl bitIndex)).toByte()
        }
        length++
    }

    fun getBit(index: Int): Boolean {
        if (index >= length) throw IndexOutOfBoundsException()
        val byteIndex = index / 8
        val bitIndex = 7 - (index % 8)  // MSB first
        return (data[byteIndex].toInt() and (1 shl bitIndex)) != 0
    }

    fun toByteArray(): ByteArray {
        val fullBytes = (length + 7) / 8
        return data.take(fullBytes).toByteArray()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BitSequence) return false
        if (length != other.length) return false

        for (i in 0 until length) {
            if (getBit(i) != other.getBit(i)) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = length
        for (i in 0 until length) {
            result = 31 * result + if (getBit(i)) 1 else 0
        }
        return result
    }

    override fun toString(): String {
        return buildString {
            for (i in indices) {
                append(if (getBit(i)) '1' else '0')
                // Optional: Add space every 8 bits for readability
                if ((i + 1) % 8 == 0 && i < length - 1) append(' ')
            }
            // Add note about any unused bits in last byte
            val unusedBits = (8 - (length % 8)) % 8
            if (unusedBits > 0) {
                append(" (+$unusedBits unused)")
            }
        }
    }

    companion object {
        fun fromPath(path: String): BitSequence {
            val bits = BitSequence()
            path.forEach { char ->
                bits.append(char == '1')
            }
            return bits
        }
    }
}