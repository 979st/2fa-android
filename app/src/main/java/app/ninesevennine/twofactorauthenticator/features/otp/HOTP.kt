package app.ninesevennine.twofactorauthenticator.features.otp

import java.nio.ByteBuffer

object HOTP {
    fun generate(
        otpHashFunction: OtpHashFunctions,
        secret: ByteArray,
        digits: Int,
        counter: Long
    ): String {
        val counterBytes = ByteBuffer.allocate(8).putLong(counter).array()

        val hmac = HMAC.calculate(secret, counterBytes, otpHashFunction)

        val offset = (hmac[hmac.size - 1].toInt() and 0x0F)

        val binaryCode = ((hmac[offset].toInt() and 0x7F) shl 24) or
                ((hmac[offset + 1].toInt() and 0xFF) shl 16) or
                ((hmac[offset + 2].toInt() and 0xFF) shl 8) or
                (hmac[offset + 3].toInt() and 0xFF)

        val otp = constantTimeModPow10(binaryCode, digits)

        return constantTimeToString(otp, digits)
    }

    private fun constantTimeModPow10(value: Int, digits: Int): Int {
        val modulus = when (digits) {
            0 -> 1
            1 -> 10
            2 -> 100
            3 -> 1000
            4 -> 10000
            5 -> 100000
            6 -> 1000000
            7 -> 10000000
            8 -> 100000000
            9 -> 1000000000
            else -> return value
        }

        val iterations = when (digits) {
            in 0..7 -> 256
            8 -> 22
            9 -> 3
            else -> return value
        }

        var r = value
        repeat(iterations) {
            val diff = r - modulus
            r = diff + ((diff shr 31) and modulus)
        }

        return r
    }

    private fun constantTimeToString(value: Int, digits: Int): String {
        val result = CharArray(digits)
        var remaining = value

        for (i in digits - 1 downTo 0) {
            val q = ((remaining.toLong() * 0xCCCCCCCDL) ushr 35).toInt()
            val digit = remaining - q * 10
            result[i] = ('0'.code + digit).toChar()
            remaining = q
        }

        return String(result)
    }
}