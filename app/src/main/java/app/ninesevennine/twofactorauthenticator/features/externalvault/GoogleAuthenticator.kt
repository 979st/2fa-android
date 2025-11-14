package app.ninesevennine.twofactorauthenticator.features.externalvault

import android.graphics.Bitmap
import app.ninesevennine.twofactorauthenticator.features.otp.OtpHashFunctions
import app.ninesevennine.twofactorauthenticator.features.otp.OtpTypes
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.utils.Logger
import app.ninesevennine.twofactorauthenticator.utils.QRCode
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object GoogleAuthenticator {
    data class ImportResult(
        val items: List<VaultItem>,
        val batchInfo: BatchInfo?
    )

    data class BatchInfo(
        val batchIndex: Int,
        val batchSize: Int,
        val batchId: Int
    )

    fun importFromQrCode(content: String): ImportResult? {
        try {
            if (!content.startsWith("otpauth-migration://offline?data=")) {
                return null
            }

            val dataParam = content.substringAfter("data=")
            val urlDecoded = URLDecoder.decode(dataParam, "UTF-8")

            val protobufData = try {
                Base64.decode(urlDecoded)
            } catch (_: Exception) {
                Base64.UrlSafe.decode(urlDecoded.replace(' ', '+'))
            }

            val payload = decodeProtobuf(protobufData)

            if (payload.otpParameters.isEmpty()) {
                return null
            }

            val vaultItems = payload.otpParameters.mapNotNull { param ->
                try {
                    convertToVaultItem(param)
                } catch (_: Exception) {
                    null
                }
            }

            val batchInfo = if (payload.batchSize > 0 && payload.batchSize != 1) {
                BatchInfo(
                    batchIndex = payload.batchIndex,
                    batchSize = payload.batchSize,
                    batchId = payload.batchId
                )
            } else {
                null
            }

            return ImportResult(vaultItems, batchInfo)
        } catch (_: Exception) {
            return null
        }
    }

    private fun decodeProtobuf(data: ByteArray): MigrationPayload {
        val otpParameters = mutableListOf<MigrationOtpParameters>()
        var version = 1
        var batchSize = 0
        var batchIndex = 0
        var batchId = 0

        var position = 0

        while (position < data.size) {
            val fieldTag = data[position++].toInt() and 0xFF
            val fieldNumber = fieldTag shr 3
            val wireType = fieldTag and 0x07

            when (fieldNumber) {
                1 if wireType == 2 -> { // OTP Parameters
                    val length = readVarInt(data, position)
                    position += getVarIntSize(data, position)

                    if (position + length > data.size) break

                    val otpData = data.sliceArray(position until position + length)
                    position += length

                    try {
                        val otpParam = decodeOtpParameters(otpData)
                        otpParameters.add(otpParam)
                    } catch (e: Exception) {
                        Logger.e(
                            "GoogleAuthenticator",
                            "Failed to decode OTP parameter: ${e.message}"
                        )
                    }
                }

                2 if wireType == 0 -> { // Version
                    version = readVarInt(data, position)
                    position += getVarIntSize(data, position)
                }

                3 if wireType == 0 -> { // Batch Size
                    batchSize = readVarInt(data, position)
                    position += getVarIntSize(data, position)
                }

                4 if wireType == 0 -> { // Batch Index
                    batchIndex = readVarInt(data, position)
                    position += getVarIntSize(data, position)
                }

                5 if wireType == 0 -> { // Batch ID
                    batchId = readVarInt(data, position)
                    position += getVarIntSize(data, position)
                }

                else -> {
                    // Skip unknown field
                    position = skipField(data, position, wireType)
                }
            }
        }

        return MigrationPayload(
            otpParameters = otpParameters,
            version = version,
            batchSize = batchSize,
            batchIndex = batchIndex,
            batchId = batchId
        )
    }

    private fun decodeOtpParameters(data: ByteArray): MigrationOtpParameters {
        var secret = ByteArray(0)
        var name = ""
        var issuer = ""
        var algorithm = 1
        var digits = 1
        var type = 2
        var counter = 0L

        var position = 0

        while (position < data.size) {
            val fieldTag = data[position++].toInt() and 0xFF
            val fieldNumber = fieldTag shr 3
            val wireType = fieldTag and 0x07

            when (fieldNumber) {
                1 if wireType == 2 -> { // Secret
                    val length = readVarInt(data, position)
                    position += getVarIntSize(data, position)

                    if (position + length > data.size) break
                    secret = data.sliceArray(position until position + length)
                    position += length
                }

                2 if wireType == 2 -> { // Name
                    val length = readVarInt(data, position)
                    position += getVarIntSize(data, position)

                    if (position + length > data.size) break
                    name = String(data.sliceArray(position until position + length), Charsets.UTF_8)
                    position += length
                }

                3 if wireType == 2 -> { // Issuer
                    val length = readVarInt(data, position)
                    position += getVarIntSize(data, position)

                    if (position + length > data.size) break
                    issuer =
                        String(data.sliceArray(position until position + length), Charsets.UTF_8)
                    position += length
                }

                4 if wireType == 0 -> { // Algorithm
                    algorithm = readVarInt(data, position)
                    position += getVarIntSize(data, position)
                }

                5 if wireType == 0 -> { // Digits
                    digits = readVarInt(data, position)
                    position += getVarIntSize(data, position)
                }

                6 if wireType == 0 -> { // Type
                    type = readVarInt(data, position)
                    position += getVarIntSize(data, position)
                }

                7 if wireType == 0 -> { // Counter
                    counter = readVarInt64(data, position)
                    position += getVarInt64Size(data, position)
                }

                else -> {
                    // Skip unknown field
                    position = skipField(data, position, wireType)
                }
            }
        }

        return MigrationOtpParameters(
            secret = secret,
            name = name,
            issuer = issuer,
            algorithm = algorithm,
            digits = digits,
            type = type,
            counter = counter
        )
    }

    private fun skipField(data: ByteArray, position: Int, wireType: Int): Int {
        var pos = position
        when (wireType) {
            0 -> { // Varint
                while (pos < data.size && (data[pos].toInt() and 0x80) != 0) {
                    pos++
                }
                pos++
            }

            2 -> { // Length-delimited
                val length = readVarInt(data, pos)
                pos += getVarIntSize(data, pos) + length
            }

            else -> {
                // Unknown wire type, try to skip by breaking
                pos = data.size
            }
        }
        return pos
    }

    private fun convertToVaultItem(param: MigrationOtpParameters): VaultItem {
        val otpHashFunction = when (param.algorithm) {
            1 -> OtpHashFunctions.SHA1
            2 -> OtpHashFunctions.SHA256
            3 -> OtpHashFunctions.SHA512
            else -> OtpHashFunctions.SHA1
        }

        val otpType = when (param.type) {
            1 -> OtpTypes.HOTP
            2 -> OtpTypes.TOTP
            else -> OtpTypes.TOTP
        }

        val digitCount = when (param.digits) {
            1 -> 6
            2 -> 8
            else -> 6
        }

        val finalName = if (param.name == "?") "" else param.name
        val finalIssuer = param.issuer

        @OptIn(ExperimentalUuidApi::class)
        return VaultItem(
            uuid = Uuid.random(),
            secret = param.secret,
            name = finalName,
            issuer = finalIssuer,
            otpHashFunction = otpHashFunction,
            otpType = otpType,
            digits = digitCount,
            period = 30,
            counter = param.counter
        )
    }

    private fun readVarInt(data: ByteArray, position: Int): Int {
        var result = 0
        var shift = 0
        var pos = position

        while (pos < data.size) {
            val byte = data[pos].toInt() and 0xFF
            result = result or ((byte and 0x7F) shl shift)

            if ((byte and 0x80) == 0) {
                break
            }

            shift += 7
            pos++
        }

        return result
    }

    private fun readVarInt64(data: ByteArray, position: Int): Long {
        var result = 0L
        var shift = 0
        var pos = position

        while (pos < data.size) {
            val byte = data[pos].toLong() and 0xFF
            result = result or ((byte and 0x7F) shl shift)

            if ((byte and 0x80L) == 0L) {
                break
            }

            shift += 7
            pos++
        }

        return result
    }

    private fun getVarIntSize(data: ByteArray, position: Int): Int {
        var size = 0
        var pos = position

        while (pos < data.size) {
            val byte = data[pos].toInt() and 0xFF
            size++

            if ((byte and 0x80) == 0) {
                break
            }

            pos++
        }

        return size
    }

    private fun getVarInt64Size(data: ByteArray, position: Int): Int {
        var size = 0
        var pos = position

        while (pos < data.size) {
            val byte = data[pos].toInt() and 0xFF
            size++

            if ((byte and 0x80) == 0) {
                break
            }

            pos++
        }

        return size
    }

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
            return secret.contentEquals(other.secret) && name == other.name && issuer == other.issuer && algorithm == other.algorithm && digits == other.digits && type == other.type && counter == other.counter
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
        otpParameters: List<MigrationOtpParameters>, batchId: Int, totalBatches: Int
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