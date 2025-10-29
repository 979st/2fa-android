package app.ninesevennine.twofactorauthenticator.utils

import org.bouncycastle.util.encoders.Base32

object Base32 {
    fun encode(data: ByteArray): String {
        if (data.isEmpty()) return ""

        return Base32.toBase32String(data).replace("=", "")
    }

    fun decode(input: String): ByteArray? {
        if (input.isEmpty()) return null

        return try {
            val normalizedInput = input.uppercase()
            val paddedInput = normalizedInput.padEnd((normalizedInput.length + 7) / 8 * 8, '=')

            Base32.decode(paddedInput)
        } catch (_: Exception) {
            null
        }
    }
}