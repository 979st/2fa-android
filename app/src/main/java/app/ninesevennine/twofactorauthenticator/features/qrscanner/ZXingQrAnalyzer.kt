package app.ninesevennine.twofactorauthenticator.features.qrscanner

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import app.ninesevennine.twofactorauthenticator.utils.Logger
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import kotlin.math.min

class ZXingQrAnalyzer(
    private val viewfinderPercent: Float,
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val reader = MultiFormatReader().apply {
        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
        )

        setHints(hints)
    }

    private var imageBuffer = ByteArray(0)
    private var packedBuffer = ByteArray(0)
    private var scanning = false

    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.format != ImageFormat.YUV_420_888) {
            imageProxy.close()
            return
        }

        imageProxy.use {
            runCatching {
                if (scanning)
                    return

                scanning = true

                val plane = it.planes[0]
                val buffer = plane.buffer
                val dataSize = buffer.remaining()

                if (imageBuffer.size < dataSize) {
                    imageBuffer = ByteArray(dataSize)
                }

                buffer.get(imageBuffer, 0, dataSize)

                val imageWidth = it.width
                val imageHeight = it.height
                val rowStride = plane.rowStride

                val yData = if (rowStride == imageWidth) {
                    imageBuffer
                } else {
                    val packedSize = imageWidth * imageHeight
                    if (packedBuffer.size < packedSize) {
                        packedBuffer = ByteArray(packedSize)
                    }

                    var srcPos = 0
                    var dstPos = 0
                    repeat(imageHeight) {
                        imageBuffer.copyInto(
                            destination = packedBuffer,
                            destinationOffset = dstPos,
                            startIndex = srcPos,
                            endIndex = srcPos + imageWidth
                        )
                        srcPos += rowStride
                        dstPos += imageWidth
                    }
                    packedBuffer
                }

                val minDim = min(imageWidth, imageHeight)
                val roiSize = (minDim * viewfinderPercent).toInt()
                val left = (imageWidth - roiSize) / 2
                val top = (imageHeight - roiSize) / 2

                val source = PlanarYUVLuminanceSource(
                    yData,
                    imageWidth,
                    imageHeight,
                    left.coerceAtLeast(0),
                    top.coerceAtLeast(0),
                    roiSize.coerceAtMost(imageWidth - left),
                    roiSize.coerceAtMost(imageHeight - top),
                    false
                )

                processImage(source)
            }.onFailure { e ->
                Logger.e("ZxingQrAnalyzer", "Analyze error: ${e.stackTraceToString()}")
            }
        }
    }

    private fun processImage(source: PlanarYUVLuminanceSource) {
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        runCatching {
            reader.decodeWithState(bitmap)?.text?.let { qrText ->
                onQrCodeScanned(qrText)
            }
        }.onFailure {
            if (it is ReaderException) {
                runCatching {
                    val inverted = BinaryBitmap(HybridBinarizer(source.invert()))
                    reader.decodeWithState(inverted)?.text?.let { qrText ->
                        onQrCodeScanned(qrText)
                    }
                }
            }
        }

        reader.reset()
        scanning = false
    }
}