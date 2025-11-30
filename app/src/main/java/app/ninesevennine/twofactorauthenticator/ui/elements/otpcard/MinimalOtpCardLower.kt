package app.ninesevennine.twofactorauthenticator.ui.elements.otpcard

import android.view.SoundEffectConstants
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import app.ninesevennine.twofactorauthenticator.configViewModel
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.utils.Clipboard
import app.ninesevennine.twofactorauthenticator.utils.Logger
import app.ninesevennine.twofactorauthenticator.vaultViewModel
import java.security.SecureRandom
import kotlin.random.Random

private val secureRandom = SecureRandom()

@Composable
fun MinimalOtpCardLower(
    item: VaultItem
) {
    val context = LocalContext.current
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val theme = context.themeViewModel

    val colors = remember(item.otpCardColor) {
        theme.getOtpCardColors(context, item.otpCardColor)
    }

    val vaultViewModel = context.vaultViewModel

    var revealed by remember { mutableStateOf(!context.configViewModel.values.requireTapToReveal) }

    val currentTimeSeconds by vaultViewModel.currentTimeSeconds.collectAsState()
    val currentCycle = currentTimeSeconds / item.period

    val otpCode = remember(item, currentCycle, revealed) {
        if (item.secret.isEmpty()) return@remember ""

        if (context.configViewModel.values.requireTapToReveal && !revealed) {
            return@remember "•".repeat(item.digits)
        }

        vaultViewModel.generateOtp(
            otpType = item.otpType,
            otpHashFunction = item.otpHashFunction,
            secret = item.secret,
            digits = item.digits,
            period = item.period,
            count = item.counter,
            currentTimeSeconds = currentCycle * item.period
        )
    }

    val formattedCode = remember(otpCode) {
        if (context.configViewModel.values.antiPixnapping && revealed) {
            val seed = secureRandom.nextLong()
            val random = Random(seed)
            val result = StringBuilder()

            val blocksToAdd = when (otpCode.length) {
                in 4..6 -> 3
                else -> 2
            }
            var blocksAdded = 0

            for (digit in otpCode) {
                result.append(digit)

                val shouldAddBlock = blocksAdded < blocksToAdd && random.nextBoolean()
                if (shouldAddBlock) {
                    result.append('∎')
                    blocksAdded++
                }
            }

            result.toString()
        } else {
            if (otpCode.length >= 6) {
                val midpoint = otpCode.length / 2
                "${otpCode.take(midpoint)} ${otpCode.substring(midpoint)}"
            } else {
                otpCode
            }
        }
    }

    val lowerModifier = Modifier
        .fillMaxWidth()
        .height(44.dp)
        .clickable {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            view.playSoundEffect(SoundEffectConstants.CLICK)

            Logger.i("MinimalOtpCardLower", "Copied OTP code to clipboard")
            if (revealed) {
                Clipboard.copy(context, "authenticator-contents", otpCode, true)
            }

            if (context.configViewModel.values.requireTapToReveal) {
                revealed = !revealed
            }
        }

    Box(
        contentAlignment = Alignment.Center,
        modifier = lowerModifier
    ) {
        Text(
            text = formattedCode,
            fontFamily = InterVariable,
            color = colors.thirdColor,
            fontWeight = FontWeight.W700,
            fontSize = if (!revealed && context.configViewModel.values.requireTapToReveal) 30.sp else 24.sp,
            maxLines = 1
        )

        if (revealed && context.configViewModel.values.antiPixnapping && formattedCode.isNotEmpty()) {
            WhiteNoiseTexture(
                otpCode = formattedCode,
                noiseColor = colors.firstColor,
                density = 0.7f
            )
        }
    }
}

@Composable
private fun WhiteNoiseTexture(
    otpCode: String,
    noiseColor: Color,
    density: Float
) {
    val width = otpCode.length * 15

    val noiseTexture = remember(otpCode) {
        generateWhiteNoiseTexture(
            color = noiseColor,
            density = density,
            imageWidth = width
        )
    }

    Image(
        bitmap = noiseTexture,
        contentDescription = null,
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 12.dp)
            .width(width.dp),
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.None
    )
}

private fun generateWhiteNoiseTexture(
    color: Color,
    density: Float,
    imageWidth: Int
): ImageBitmap {
    val imageHeight = 30
    val colorArgb = color.toArgb()
    val androidBitmap = createBitmap(imageWidth, imageHeight)

    val seed = secureRandom.nextLong()
    val random = Random(seed)

    if (density > 0.5f) {
        androidBitmap.eraseColor(colorArgb)
        val pixels = IntArray((imageWidth * imageHeight * (1f - density)).toInt().coerceAtLeast(1))

        var i = 0
        while (i < pixels.size) {
            val x = random.nextInt(0, imageWidth)
            val y = random.nextInt(0, imageHeight)
            androidBitmap[x, y] = 0
            i++
        }
    } else {
        val pixelsToSet = (imageWidth * imageHeight * density).toInt()

        repeat(pixelsToSet) {
            val x = random.nextInt(0, imageWidth)
            val y = random.nextInt(0, imageHeight)
            androidBitmap[x, y] = colorArgb
        }
    }

    return androidBitmap.asImageBitmap()
}