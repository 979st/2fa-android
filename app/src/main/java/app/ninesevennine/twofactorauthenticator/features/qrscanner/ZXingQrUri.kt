package app.ninesevennine.twofactorauthenticator.features.qrscanner

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ZXingQrUri {
    suspend fun decode(uri: Uri, contentResolver: ContentResolver): String? {
        val bitmap = withContext(Dispatchers.IO) {
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } ?: return null

        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        bitmap.recycle()

        return withContext(Dispatchers.Default) {
            runCatching {
                val source = RGBLuminanceSource(width, height, pixels)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

                val reader = MultiFormatReader().apply {
                    setHints(mapOf(
                        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                        DecodeHintType.TRY_HARDER to true
                    ))
                }

                reader.decodeWithState(binaryBitmap)?.text
            }.getOrNull()
        }
    }
}