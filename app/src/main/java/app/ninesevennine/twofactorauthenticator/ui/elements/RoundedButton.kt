package app.ninesevennine.twofactorauthenticator.ui.elements

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.themeViewModel

@Composable
fun RoundedButton(
    label: String,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    val shape = remember { RoundedCornerShape(28.dp) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(color = colors.background, shape = shape)
            .border(width = 1.dp, color = colors.outline, shape = shape)
            .clip(shape)
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    view.playSoundEffect(SoundEffectConstants.CLICK)

                    onClick()
                },
                indication = ripple(color = colors.onBackground.copy(alpha = 0.08f)),
                interactionSource = interactionSource
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = colors.onBackground,
            fontSize = 14.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W700,
            fontFamily = InterVariable,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}