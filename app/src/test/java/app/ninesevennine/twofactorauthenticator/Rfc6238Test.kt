package app.ninesevennine.twofactorauthenticator

import app.ninesevennine.twofactorauthenticator.features.otp.HOTP
import app.ninesevennine.twofactorauthenticator.features.otp.OtpHashFunctions
import app.ninesevennine.twofactorauthenticator.features.otp.TOTP
import org.junit.Assert.assertEquals
import org.junit.Test

// Appendix B.  Test Vectors
class Rfc6238Test {
    private val secretSha1 = "12345678901234567890".toByteArray(Charsets.US_ASCII)
    private val secretSha256 = "12345678901234567890123456789012".toByteArray(Charsets.US_ASCII)
    private val secretSha512 =
        "1234567890123456789012345678901234567890123456789012345678901234".toByteArray(Charsets.US_ASCII)

    private val period = 30

    @Test
    fun sha1_time() {
        val testCases = listOf(
            59L to "94287082",
            1111111109L to "07081804",
            1111111111L to "14050471",
            1234567890L to "89005924",
            2000000000L to "69279037",
            20000000000L to "65353130"
        )

        testCases.forEach { (time, expected) ->
            val otp = TOTP.generate(
                otpHashFunction = OtpHashFunctions.SHA1,
                secret = secretSha1,
                digits = 8,
                period = period,
                currentTimeSeconds = time
            )
            assertEquals("Time: $time", otp, expected)
        }
    }

    @Test
    fun sha1_T() {
        val testCases = listOf(
            0x0000000000000001L to "94287082",
            0x00000000023523ECL to "07081804",
            0x00000000023523EDL to "14050471",
            0x000000000273EF07L to "89005924",
            0x0000000003F940AAL to "69279037",
            0x0000000027BC86AAL to "65353130"
        )

        testCases.forEach { (counter, expected) ->
            val otp = HOTP.generate(OtpHashFunctions.SHA1, secretSha1, 8, counter)
            assertEquals("Counter: $counter", otp, expected)
        }
    }

    @Test
    fun sha256_T() {
        val testCases = listOf(
            0x0000000000000001L to "46119246",
            0x00000000023523ECL to "68084774",
            0x00000000023523EDL to "67062674",
            0x000000000273EF07L to "91819424",
            0x0000000003F940AAL to "90698825",
            0x0000000027BC86AAL to "77737706"
        )

        testCases.forEach { (counter, expected) ->
            val otp = HOTP.generate(OtpHashFunctions.SHA256, secretSha256, 8, counter)
            assertEquals("Counter: $counter", otp, expected)
        }
    }

    @Test
    fun sha512_T() {
        val testCases = listOf(
            0x0000000000000001L to "90693936",
            0x00000000023523ECL to "25091201",
            0x00000000023523EDL to "99943326",
            0x000000000273EF07L to "93441116",
            0x0000000003F940AAL to "38618901",
            0x0000000027BC86AAL to "47863826"
        )

        testCases.forEach { (counter, expected) ->
            val otp = HOTP.generate(OtpHashFunctions.SHA512, secretSha512, 8, counter)
            assertEquals("Counter: $counter", otp, expected)
        }
    }
}