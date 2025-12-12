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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.themeViewModel
import kotlinx.coroutines.delay

@Composable
fun SettingsAppBar() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val colors = context.themeViewModel.colors
    val navController = LocalNavController.current

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 4.dp

    var animate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        animate = true
    }

    val shape = remember { RoundedCornerShape(28.dp) }
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Start,
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
                        shape = shape
                    )
                    .background(
                        color = colors.background,
                        shape = shape
                    )
                    .border(width = 1.dp, color = colors.outline, shape = shape)
                    .clickable(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            view.playSoundEffect(SoundEffectConstants.CLICK)

                            navController.popBackStack()
                        },
                        indication = ripple(color = colors.onBackground.copy(alpha = 0.08f)),
                        interactionSource = interactionSource
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(35.dp)
                )
            }
        }
    }
}