package app.ninesevennine.twofactorauthenticator.ui.elements

import android.view.SoundEffectConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.OtpCardColors

@Composable
fun ItemColorOption(
    currentOtpCardColor: OtpCardColors,
    otpCardColor: OtpCardColors,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val theme = context.themeViewModel
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val palette = remember(otpCardColor) {
        theme.getOtpCardColors(context, otpCardColor)
    }

    Canvas(
        Modifier.size(54.dp).clickable {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            view.playSoundEffect(SoundEffectConstants.CLICK)

            onClick()
        }
    ) {
        val outerColor = palette.secondColor
        val innerColor = if (otpCardColor == currentOtpCardColor) palette.firstColor else null

        val rOuter = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = outerColor, center = center, radius = rOuter)
        innerColor?.let { drawCircle(color = it, center = center, radius = rOuter * 0.2f) }
    }
}