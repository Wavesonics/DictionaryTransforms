import java.io.BufferedInputStream
import java.util.PriorityQueue

class HuffmanEncoder {
    internal val charToCodeMap: MutableMap<Char, BitSequence> = mutableMapOf()
    private var root: Node

    constructor(charFrequency: Map<Char, Int>) {
        val charFreqMap = charFrequency.toMutableMap()
        if (charFreqMap.containsKey(STREAM_DELIMITER).not()) {
            charFreqMap[STREAM_DELIMITER] = charFreqMap.values.max() / 2
        }

        val priorityQueue = PriorityQueue(compareBy<Node> { it.frequency })
        charFreqMap.forEach { (char, frequency) ->
            priorityQueue.add(Node(char, frequency))
        }

        while (priorityQueue.size > 1) {
            val left = priorityQueue.poll()
            val right = priorityQueue.poll()
            val parent = Node(null, left.frequency + right.frequency, left, right)
            priorityQueue.add(parent)
        }

        root = priorityQueue.poll()
        buildCharToCodeMap(root, "")
    }

    constructor(charFrequencyByteArray: ByteArray) {
        importCharToCodeMap(charFrequencyByteArray)
        root = reconstructTreeFromCodes()
    }

    fun encode(input: String): ByteArray {
        val encodedBits = BitSequence()

        // Debug: Print character frequencies in input
        //val actualFreqs = input.groupBy { it }.mapValues { it.value.size }
        //println("Actual character frequencies in input: $actualFreqs")

        // Debug: Print Huffman codes
        //println("Huffman codes:")
//        charToCodeMap.forEach { (char, bits) ->
//            val bitsString = buildString {
//                for (i in 0 until bits.length) {
//                    append(if (bits.getBit(i)) '1' else '0')
//                }
//            }
//            //println("'$char': $bitsString (${bits.length} bits)")
//        }

        // Ensure input ends with delimiter if not already present
        val inputWithDelimiter = if (input.endsWith(STREAM_DELIMITER)) input else input + STREAM_DELIMITER

        // Calculate total bits needed (for debug output only)
        var totalBits = 0
        inputWithDelimiter.forEach { char ->
            val code = charToCodeMap[char] ?: throw IllegalArgumentException("Character '$char' is not in the Huffman tree!")
            totalBits += code.length
        }

        //println("Input size in bits: ${input.length * 8}")
        //println("Encoded size in bits: $totalBits")

        // Encode the actual data
        inputWithDelimiter.forEach { char ->
            val code = charToCodeMap[char] ?: throw IllegalArgumentException("Character '$char' is not in the Huffman tree!")
            for (i in 0 until code.length) {
                encodedBits.append(code.getBit(i))
            }
        }

        return encodedBits.toByteArray()
    }

    fun decode(encoded: ByteArray): String {
        val decoded = decodeFromBits(encoded)
        // Remove the delimiter if it exists at the end
        return if (decoded.endsWith(STREAM_DELIMITER)) decoded.dropLast(1) else decoded
    }

    fun decode(inputStream: BufferedInputStream, decodeBuffer: CharArray = CharArray(64)): String {
        var bufferPosition = 0
        var currentNode = root

        while (true) {
            val byte = inputStream.read()
            if (byte == -1) break

            val currentByte = byte and 0xFF
            var bitPosition = 0

            while (bitPosition < 8) {
                currentNode = if ((currentByte and BIT_MASKS[bitPosition]) != 0) {
                    currentNode.right
                } else {
                    currentNode.left
                } ?: error("Invalid Huffman encoding")

                currentNode.char?.let { char ->
                    if (char == STREAM_DELIMITER) return String(decodeBuffer, 0, bufferPosition)
                    if (bufferPosition >= decodeBuffer.size) error("Buffer was not large enough")

                    decodeBuffer[bufferPosition++] = char
                    currentNode = root  // Reset to root, which we know is non-null
                }

                bitPosition++
            }
        }

        error("Input stream ended before finding delimiter")
    }

    private fun decodeFromBits(encodedBytes: ByteArray): String {
        val result = StringBuilder()
        var currentNode: Node? = root
        var bitIndex = 0
        val totalBits = encodedBytes.size * 8

        while (bitIndex < totalBits) {
            val byteIndex = bitIndex / 8
            val currentBit = (encodedBytes[byteIndex].toInt() and (1 shl (7 - (bitIndex % 8)))) != 0

            currentNode = if (currentBit) currentNode?.right else currentNode?.left
                ?: throw IllegalStateException("Invalid Huffman encoding")

            currentNode?.char?.let { char ->
                result.append(char)
                if (char == STREAM_DELIMITER) {
                    return result.toString()
                }
                currentNode = root
            }

            bitIndex++
        }

        throw IllegalStateException("Encoded data did not contain delimiter character")
    }

    private fun buildCharToCodeMap(node: Node?, path: String) {
        if (node == null) return

        node.char?.let { char ->
            charToCodeMap[char] = BitSequence.fromPath(path)
        }

        buildCharToCodeMap(node.left, path + "0")
        buildCharToCodeMap(node.right, path + "1")
    }

    private fun reconstructTreeFromCodes(): Node {
        // Start with a mutable root node
        val root = MutableNode()

        // Build the tree by following each code path
        charToCodeMap.forEach { (char, bits) ->
            var current = root

            // Follow/create the path for each bit
            for (i in 0 until bits.length) {
                val isLast = i == bits.length - 1
                val goRight = bits.getBit(i)

                if (goRight) {
                    if (current.right == null) {
                        current.right = MutableNode()
                    }
                    current = current.right!!
                } else {
                    if (current.left == null) {
                        current.left = MutableNode()
                    }
                    current = current.left!!
                }

                // Set the character at the leaf node
                if (isLast) {
                    current.char = char
                }
            }
        }

        // Convert the mutable tree to an immutable one
        return root.toImmutableNode()
    }

    fun exportCharToCodeMap(): ByteArray {
        // Convert BitSequence to string representation for serialization
        val serializedMap = charToCodeMap.entries.joinToString(separator = ";") { (char, bits) ->
            val bitsString = buildString {
                for (i in 0 until bits.length) {
                    append(if (bits.getBit(i)) '1' else '0')
                }
            }
            //println("Exporting char '$char' with bits: $bits (as string: $bitsString)")
            "$char:$bitsString"
        }
        return serializedMap.toByteArray(Charsets.UTF_8)
    }

    fun importCharToCodeMap(byteArray: ByteArray) {
        val serializedMap = byteArray.toString(Charsets.UTF_8)
        //println("Importing serialized map: $serializedMap")
        charToCodeMap.clear()
        serializedMap.split(";").forEach { entry ->
            val (char, code) = entry.split(":")
            val bits = BitSequence.fromPath(code)
            //println("Importing char '${char[0]}' with code $code, resulting bits: $bits")
            charToCodeMap[char[0]] = bits
        }
        root = reconstructTreeFromCodes()
    }

    // Mutable node class for tree construction
    private class MutableNode {
        var char: Char? = null
        var left: MutableNode? = null
        var right: MutableNode? = null

        fun toImmutableNode(): Node {
            return Node(
                char = char,
                frequency = 0, // Frequency not needed for reconstruction
                left = left?.toImmutableNode(),
                right = right?.toImmutableNode()
            )
        }
    }

    private class Node(
        val char: Char?, // Immutable
        val frequency: Int, // Immutable
        val left: Node? = null, // Immutable
        val right: Node? = null // Immutable
    )

    companion object {
        const val STREAM_DELIMITER: Char = '\n'

        private val BIT_MASKS = intArrayOf(
            0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01
        )
    }
}