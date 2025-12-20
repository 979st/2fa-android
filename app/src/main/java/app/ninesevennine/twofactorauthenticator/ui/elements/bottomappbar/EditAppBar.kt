package app.ninesevennine.twofactorauthenticator.ui.elements.bottomappbar

import android.view.SoundEffectConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import app.ninesevennine.twofactorauthenticator.themeViewModel
import kotlinx.coroutines.delay

@Composable
fun EditAppBar(
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val colors = context.themeViewModel.colors

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 4.dp

    var animate by remember { mutableStateOf(false) }
    var animate2 by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        animate = true
        delay(100)
        animate2 = true
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        AnimatedVisibility(
            visible = animate,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight / 2 },
                animationSpec = spring(stiffness = 600f, dampingRatio = 0.75f)
            ) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(stiffness = 600f, dampingRatio = 0.75f)
            ) + fadeIn(animationSpec = tween(durationMillis = 200))
        ) {
            Box(
                modifier = Modifier
                    .padding(start = navPadding, bottom = navPadding)
                    .size(56.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .background(
                        color = colors.background,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(28.dp))
                    .clickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            view.playSoundEffect(SoundEffectConstants.CLICK)

                            onCancel()
                        },
                        indication = ripple(color = colors.onBackground.copy(alpha = 0.08f)),
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = colors.onBackground,
                    modifier = Modifier.size(35.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = animate2,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight / 2 },
                animationSpec = spring(stiffness = 600f, dampingRatio = 0.75f)
            ) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(stiffness = 600f, dampingRatio = 0.75f)
            ) + fadeIn(animationSpec = tween(durationMillis = 200))
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = navPadding, end = navPadding)
                    .size(56.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .background(
                        color = colors.primary,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(28.dp))
                    .clickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            view.playSoundEffect(SoundEffectConstants.CLICK)

                            onDone()
                        },
                        indication = ripple(color = colors.onPrimary.copy(alpha = 0.08f)),
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(35.dp)
                )
            }
        }
    }
}