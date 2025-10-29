package app.ninesevennine.twofactorauthenticator.features.otp

object HOTP {
    fun generate(
        otpHashFunction: OtpHashFunctions,
        secret: ByteArray,
        digits: Int,
        counter: Long
    ): String {
        val counterBuffer = ByteArray(8)
        for (i in 7 downTo 0) {
            val bitShift = (7 - i) shl 3
            counterBuffer[i] = ((counter ushr bitShift) and 0xFF).toByte()
        }

        val hmacResult = HMAC.calculate(secret, counterBuffer, otpHashFunction)
        val dynamicOffset = hmacResult.last().toInt() and 0x0F

        val truncatedBinary =
            ((hmacResult[dynamicOffset].toInt() and 0x7F) shl 24) or
            ((hmacResult[dynamicOffset + 1].toInt() and 0xFF) shl 16) or
            ((hmacResult[dynamicOffset + 2].toInt() and 0xFF) shl 8) or
            (hmacResult[dynamicOffset + 3].toInt() and 0xFF)

        val modulusBase = pow10Bitwise(digits)
        val otpValue = modBinary(truncatedBinary, modulusBase)

        return padWithZeros(otpValue, digits)
    }

    private fun pow10Bitwise(exponent: Int): Int {
        var result = 1
        var i = 0
        while (i < exponent) {
            result = (result shl 3) + (result shl 1)
            i++
        }
        return result
    }

    private fun modBinary(value: Int, modulus: Int): Int {
        var remainder = value
        while (remainder >= modulus) {
            remainder -= modulus
        }
        return remainder
    }

    private fun padWithZeros(number: Int, targetLength: Int): String {
        val numberStr = number.toString()
        val padding = targetLength - numberStr.length
        if (padding <= 0) return numberStr

        val builder = StringBuilder(targetLength)
        repeat(padding) { builder.append('0') }
        builder.append(numberStr)
        return builder.toString()
    }
}