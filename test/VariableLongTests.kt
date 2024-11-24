import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class VariableLengthEncodingTests {

    @Test
    fun testEncodeDecodeVariableLength() {
        val testValues = listOf<Long>(
            0L,
            1L,
            127L,
            128L,
            300L,
            16384L,
            2097151L,
            123456789L,
            Long.MAX_VALUE
        )

        for (value in testValues) {
            val encoded = encodeVariableLengthLong(value)
            val decoded = decodeVariableLengthLong(ByteArrayInputStream(encoded))
            assertEquals("Failed for value: $value", value, decoded)
        }
    }

    @Test
    fun testEncodeVariableLengthSingleByte() {
        val value = 127L // Should fit into a single byte
        val encoded = encodeVariableLengthLong(value)
        assertEquals("Encoded length for 127 should be 1 byte", 1, encoded.size)
    }

    @Test
    fun testEncodeVariableLengthMultiByte() {
        val value = 300L // Should require two bytes
        val encoded = encodeVariableLengthLong(value)
        assertEquals("Encoded length for 300 should be 2 bytes", 2, encoded.size)
    }

    @Test
    fun testDecodeVariableLength() {
        val value = Long.MAX_VALUE
        val encoded = encodeVariableLengthLong(value)
        val decoded = decodeVariableLengthLong(ByteArrayInputStream(encoded))
        assertEquals("Decoded value should match original", value, decoded)
    }

    @Test
    fun testDecodeVariableLength2() {
        val value = 76520L
        val encoded = encodeVariableLengthLong(value)
        val decoded = decodeVariableLengthLong(ByteArrayInputStream(encoded))
        assertEquals("Decoded value should match original", value, decoded)
    }

    @Test(expected = java.io.EOFException::class)
    fun testDecodeVariableLengthInvalidInput() {
        val invalidInput = byteArrayOf(0x80.toByte()) // Invalid because it's expecting continuation but there is none
        decodeVariableLengthLong(ByteArrayInputStream(invalidInput))
    }
}