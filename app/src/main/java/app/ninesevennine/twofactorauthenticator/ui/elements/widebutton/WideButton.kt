package app.ninesevennine.twofactorauthenticator.ui.elements.widebutton

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.themeViewModel

@Composable
fun WideButton(
    modifier: Modifier = Modifier,
    label: String,
    color: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val colors = context.themeViewModel.colors

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = if (color == Color.Unspecified) colors.primaryContainer else color,
                shape = RoundedCornerShape(28.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.playSoundEffect(SoundEffectConstants.CLICK)

                onClick()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = label,
                fontFamily = InterVariable,
                color = if (textColor == Color.Unspecified) colors.onPrimaryContainer else textColor,
                fontWeight = FontWeight.W700,
                fontSize = 16.sp,
                maxLines = 1
            )
        }
    }
}