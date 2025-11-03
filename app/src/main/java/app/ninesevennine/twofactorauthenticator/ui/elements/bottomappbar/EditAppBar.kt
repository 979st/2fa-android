package app.ninesevennine.twofactorauthenticator.ui.elements.bottomappbar

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import app.ninesevennine.twofactorauthenticator.themeViewModel

@Composable
fun EditAppBar(
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val colors = context.themeViewModel.colors

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = navBottom + 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .padding(horizontal = navBottom + 4.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(26.dp),
                    clip = false
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 26.dp, bottomStart = 26.dp
                        )
                    )
                    .background(colors.primaryContainer)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        view.playSoundEffect(SoundEffectConstants.CLICK)

                        onCancel()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = colors.onPrimaryContainer
                )
            }

            Box(
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f)
                    .clip(
                        RoundedCornerShape(
                            topEnd = 26.dp, bottomEnd = 26.dp
                        )
                    )
                    .background(colors.primary)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        view.playSoundEffect(SoundEffectConstants.CLICK)

                        onDone()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = colors.onPrimary
                )
            }
        }
    }
}