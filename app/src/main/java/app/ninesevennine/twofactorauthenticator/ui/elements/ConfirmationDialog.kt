package app.ninesevennine.twofactorauthenticator.ui.elements

import android.view.SoundEffectConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.themeViewModel
import kotlinx.coroutines.delay

@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    title: String,
    message: String,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    confirmButtonColor: Color? = null,
    isDestructive: Boolean = false
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismissRequest) {
            DialogContent(
                onDismissRequest = onDismissRequest,
                icon = icon,
                title = title,
                message = message,
                confirmButtonText = confirmButtonText,
                dismissButtonText = dismissButtonText,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
                confirmButtonColor = confirmButtonColor,
                isDestructive = isDestructive
            )
        }
    }
}

@Composable
private fun DialogContent(
    onDismissRequest: () -> Unit,
    icon: @Composable (() -> Unit)?,
    title: String,
    message: String,
    confirmButtonText: String,
    dismissButtonText: String?,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)?,
    confirmButtonColor: Color?,
    isDestructive: Boolean
) {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    var confirmPressed by remember { mutableStateOf(false) }
    var dismissPressed by remember { mutableStateOf(false) }

    // Handle confirm action
    LaunchedEffect(confirmPressed) {
        if (confirmPressed) {
            delay(100)
            onConfirm()
            confirmPressed = false
        }
    }

    // Handle dismiss action
    LaunchedEffect(dismissPressed) {
        if (dismissPressed) {
            delay(100)
            onDismiss?.invoke() ?: onDismissRequest()
            dismissPressed = false
        }
    }

    // Animation scales for buttons
    val confirmScale by animateFloatAsState(
        targetValue = if (confirmPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "confirmScale"
    )

    val dismissScale by animateFloatAsState(
        targetValue = if (dismissPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dismissScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(color = colors.background, shape = RoundedCornerShape(24.dp))
            .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon?.invoke() ?: run {
            if (isDestructive) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            color = colors.onBackground,
            fontSize = 16.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W700,
            fontFamily = InterVariable,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            modifier = Modifier.fillMaxWidth(),
            color = colors.onBackground,
            fontSize = 15.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W500,
            fontFamily = InterVariable,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (dismissButtonText != null) {
                DialogButton(
                    text = dismissButtonText,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        dismissPressed = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .scale(dismissScale),
                    backgroundColor = colors.background,
                    textColor = colors.onBackground,
                    hasBorder = true
                )
            }

            DialogButton(
                text = confirmButtonText,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    confirmPressed = true
                },
                modifier = Modifier
                    .weight(1f)
                    .scale(confirmScale),
                backgroundColor = confirmButtonColor ?: colors.primary,
                textColor = if (confirmButtonColor != null) colors.onPrimary else colors.onSurface,
                hasBorder = true
            )
        }
    }
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    textColor: Color,
    hasBorder: Boolean = false
) {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .then(
                if (hasBorder) {
                    Modifier.border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(28.dp))
                } else {
                    Modifier
                }
            )
            .background(color = backgroundColor, shape = RoundedCornerShape(28.dp))
            .clickable(
                onClick = onClick,
                indication = ripple(color = colors.onBackground.copy(alpha = 0.08f)),
                interactionSource = interactionSource
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontStyle = FontStyle.Normal,
            fontWeight = FontWeight.W700,
            fontFamily = InterVariable
        )
    }
}