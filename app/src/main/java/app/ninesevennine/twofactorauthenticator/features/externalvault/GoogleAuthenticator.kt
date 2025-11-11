package app.ninesevennine.twofactorauthenticator.features.externalvault

import android.graphics.Bitmap
import app.ninesevennine.twofactorauthenticator.features.otp.OtpHashFunctions
import app.ninesevennine.twofactorauthenticator.features.otp.OtpTypes
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.utils.Logger
import app.ninesevennine.twofactorauthenticator.utils.QRCode
import java.net.URLEncoder
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object GoogleAuthenticator {
    fun exportVaultItems(vaultItems: List<VaultItem>): List<Bitmap> {
        Logger.i("GoogleAuthenticator", "exportVaultItems")

        if (vaultItems.isEmpty()) {
            Logger.i("GoogleAuthenticator", "No items to export")
            return emptyList()
        }

        // Filter compatible items
        val compatibleItems = vaultItems.filter { isCompatible(it) }

        if (compatibleItems.isEmpty()) {
            Logger.i("GoogleAuthenticator", "No compatible items to export")
            return emptyList()
        }

        Logger.i(
            "GoogleAuthenticator",
            "${compatibleItems.size} / ${vaultItems.size} items can be exported to Google Authenticator"
        )

        // Convert to migration parameters
        val otpParameters = compatibleItems.map { convertToMigrationParameters(it) }

        // Try to fit all in one QR code first
        val singlePayload = MigrationPayload(
            otpParameters = otpParameters,
            version = 1,
            batchSize = 1,
            batchIndex = 0,
            batchId = (System.currentTimeMillis() / 1000).toInt()
        )

        val singleUrl = createMigrationUrl(singlePayload)
        QRCode.generateVersion15(singleUrl)?.let { bitmap ->
            // All items fit in one QR code
            return listOf(bitmap)
        }

        // Split into multiple QR codes
        return createMultipleQRCodes(otpParameters)
    }

    private data class MigrationOtpParameters(
        val secret: ByteArray,
        val name: String,
        val issuer: String,
        val algorithm: Int = 1,
        val digits: Int = 6,
        val type: Int = 2,
        val counter: Long = 0
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MigrationOtpParameters
            return secret.contentEquals(other.secret) &&
                    name == other.name &&
                    issuer == other.issuer &&
                    algorithm == other.algorithm &&
                    digits == other.digits &&
                    type == other.type &&
                    counter == other.counter
        }

        override fun hashCode(): Int {
            var result = secret.contentHashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + issuer.hashCode()
            result = 31 * result + algorithm
            result = 31 * result + digits
            result = 31 * result + type
            result = 31 * result + counter.hashCode()
            return result
        }
    }

    private data class MigrationPayload(
        val otpParameters: List<MigrationOtpParameters>,
        val version: Int = 1,
        val batchSize: Int = 1,
        val batchIndex: Int = 0,
        val batchId: Int = 0
    )

    private fun isCompatible(item: VaultItem): Boolean {
        // Only TOTP and HOTP
        if (item.otpType != OtpTypes.TOTP && item.otpType != OtpTypes.HOTP) return false

        // Only 6 or 8 digit codes
        if (item.digits != 6 && item.digits != 8) return false

        // Only 30-second intervals for TOTP
        if (item.otpType == OtpTypes.TOTP && item.period != 30) return false

        return true
    }

    private fun convertToMigrationParameters(item: VaultItem): MigrationOtpParameters {
        val algorithm = when (item.otpHashFunction) {
            OtpHashFunctions.SHA1 -> 1
            OtpHashFunctions.SHA256 -> 2
            OtpHashFunctions.SHA512 -> 3
        }

        val type = when (item.otpType) {
            OtpTypes.HOTP -> 1
            OtpTypes.TOTP -> 2
        }

        val digitCount = when (item.digits) {
            6 -> 1
            8 -> 2
            else -> 1
        }

        val (finalName, finalIssuer) = when {
            item.name.isBlank() && item.issuer.isBlank() -> "?" to ""
            item.name.isBlank() && item.issuer.isNotBlank() -> item.issuer to ""
            else -> item.name to item.issuer
        }

        return MigrationOtpParameters(
            secret = item.secret,
            name = finalName,
            issuer = finalIssuer,
            algorithm = algorithm,
            digits = digitCount,
            type = type,
            counter = item.counter
        )
    }

    private fun createMultipleQRCodes(otpParameters: List<MigrationOtpParameters>): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val batchId = (System.currentTimeMillis() / 1000).toInt()

        // Start with all items and progressively reduce batch size until QR generation succeeds
        val remainingItems = otpParameters.toMutableList()
        var batchIndex = 0

        while (remainingItems.isNotEmpty()) {
            var batchSize = remainingItems.size
            var batch: List<MigrationOtpParameters>
            var bitmap: Bitmap? = null

            // Try progressively smaller batch sizes until one works
            while (batchSize > 0 && bitmap == null) {
                batch = remainingItems.take(batchSize)

                val payload = MigrationPayload(
                    otpParameters = batch,
                    version = 1,
                    batchSize = -1, // Will be updated after we know total batches
                    batchIndex = batchIndex,
                    batchId = batchId
                )

                val url = createMigrationUrl(payload)
                bitmap = QRCode.generateVersion15(url)

                if (bitmap == null) {
                    batchSize--
                }
            }

            if (bitmap != null) {
                bitmaps.add(bitmap)
                // Remove processed items
                repeat(batchSize) { remainingItems.removeAt(0) }
                batchIndex++
            } else {
                // Even single item doesn't fit, skip it
                remainingItems.removeAt(0)
            }
        }

        // Update batch sizes in all QR codes (regenerate with correct batch size)
        if (bitmaps.size > 1) {
            return regenerateWithCorrectBatchSizes(otpParameters, batchId, bitmaps.size)
        }

        return bitmaps
    }

    private fun regenerateWithCorrectBatchSizes(
        otpParameters: List<MigrationOtpParameters>,
        batchId: Int,
        totalBatches: Int
    ): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val remainingItems = otpParameters.toMutableList()
        var batchIndex = 0

        while (remainingItems.isNotEmpty() && batchIndex < totalBatches) {
            var batchSize = remainingItems.size
            var batch: List<MigrationOtpParameters>
            var bitmap: Bitmap? = null

            while (batchSize > 0 && bitmap == null) {
                batch = remainingItems.take(batchSize)

                val payload = MigrationPayload(
                    otpParameters = batch,
                    version = 1,
                    batchSize = totalBatches,
                    batchIndex = batchIndex,
                    batchId = batchId
                )

                val url = createMigrationUrl(payload)
                bitmap = QRCode.generateVersion15(url)

                if (bitmap == null) {
                    batchSize--
                }
            }

            if (bitmap != null) {
                bitmaps.add(bitmap)
                repeat(batchSize) { remainingItems.removeAt(0) }
            }

            batchIndex++
        }

        return bitmaps
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun createMigrationUrl(payload: MigrationPayload): String {
        val protobufData = encodeProtobuf(payload)
        val base64Data = Base64.encode(protobufData)
        val urlEncodedData = URLEncoder.encode(base64Data, "UTF-8")
        return "otpauth-migration://offline?data=$urlEncodedData"
    }

    private fun encodeProtobuf(payload: MigrationPayload): ByteArray {
        val buffer = mutableListOf<Byte>()

        payload.otpParameters.forEach { otp ->
            buffer.add(0x0A.toByte())

            val otpBuffer = mutableListOf<Byte>()

            otpBuffer.add(0x0A.toByte())
            writeVarInt(otpBuffer, otp.secret.size)
            otpBuffer.addAll(otp.secret.toList())

            val nameBytes = otp.name.toByteArray(Charsets.UTF_8)
            otpBuffer.add(0x12.toByte())
            writeVarInt(otpBuffer, nameBytes.size)
            otpBuffer.addAll(nameBytes.toList())

            val issuerBytes = otp.issuer.toByteArray(Charsets.UTF_8)
            otpBuffer.add(0x1A.toByte())
            writeVarInt(otpBuffer, issuerBytes.size)
            otpBuffer.addAll(issuerBytes.toList())

            otpBuffer.add(0x20.toByte())
            writeVarInt(otpBuffer, otp.algorithm)

            otpBuffer.add(0x28.toByte())
            writeVarInt(otpBuffer, otp.digits)

            otpBuffer.add(0x30.toByte())
            writeVarInt(otpBuffer, otp.type)

            if (otp.type == 1) {
                otpBuffer.add(0x38.toByte())
                writeVarInt64(otpBuffer, otp.counter)
            }

            writeVarInt(buffer, otpBuffer.size)
            buffer.addAll(otpBuffer)
        }

        buffer.add(0x10.toByte())
        writeVarInt(buffer, payload.version)

        buffer.add(0x18.toByte())
        writeVarInt(buffer, payload.batchSize)

        buffer.add(0x20.toByte())
        writeVarInt(buffer, payload.batchIndex)

        buffer.add(0x28.toByte())
        writeVarInt(buffer, payload.batchId)

        return buffer.toByteArray()
    }

    private fun writeVarInt(buffer: MutableList<Byte>, value: Int) {
        var v = value
        while (v >= 0x80) {
            buffer.add(((v and 0x7F) or 0x80).toByte())
            v = v ushr 7
        }
        buffer.add((v and 0x7F).toByte())
    }

    private fun writeVarInt64(buffer: MutableList<Byte>, value: Long) {
        var v = value
        while (v >= 0x80L) {
            buffer.add(((v and 0x7FL) or 0x80L).toByte())
            v = v ushr 7
        }
        buffer.add((v and 0x7FL).toByte())
    }
}