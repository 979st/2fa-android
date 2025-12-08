package app.ninesevennine.twofactorauthenticator.ui.elements

import android.view.SoundEffectConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.themeViewModel
import kotlinx.coroutines.delay

@Composable
fun RoundedRefreshButton(
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    label: String,
    spinDegrees: Float = 360f,
    spinDurationMs: Int = 600,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    var cumulativeRotation by remember { mutableFloatStateOf(0f) }
    val iconRotation by animateFloatAsState(
        targetValue = cumulativeRotation,
        animationSpec = tween(durationMillis = spinDurationMs),
        label = "iconRotation"
    )

    LaunchedEffect(Unit) {
        cumulativeRotation += spinDegrees
    }

    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(250)
            pressed = false
        }
    }

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "backgroundAlpha"
    )

    val shape = remember { RoundedCornerShape(28.dp) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    view.playSoundEffect(SoundEffectConstants.CLICK)

                    cumulativeRotation += spinDegrees
                    pressed = true

                    onClick()
                },
                indication = null,
                interactionSource = interactionSource
            ),
        contentAlignment = Alignment.Center
    ) {
        if (backgroundAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(shape)
                    .background(
                        color = colors.onBackground.copy(alpha = 0.08f * backgroundAlpha),
                        shape = shape
                    )
                    .border(width = 1.dp, color = colors.outline.copy(alpha = backgroundAlpha), shape = shape)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val iconModifier = Modifier.size(35.dp).graphicsLayer( rotationZ = iconRotation )

            when {
                painter != null -> {
                    Icon(
                        painter = painter,
                        contentDescription = null,
                        modifier = iconModifier,
                        tint = Color.Unspecified
                    )
                }

                imageVector != null -> {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = null,
                        modifier = iconModifier,
                        tint = colors.onBackground
                    )
                }

                else -> {
                    Icon(
                        painter = painterResource(R.drawable.test),
                        contentDescription = null,
                        modifier = iconModifier,
                        tint = Color.Unspecified
                    )
                }
            }

            Text(
                text = label,
                modifier = Modifier.weight(1f),
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
}