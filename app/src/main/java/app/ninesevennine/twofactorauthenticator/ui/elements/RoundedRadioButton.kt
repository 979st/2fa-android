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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

@Composable
fun RoundedRadioButton(
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    label: String,
    enabled: Boolean = false,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current

    val checkmarkScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "checkmarkScale"
    )

    val backgroundScale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "backgroundScale"
    )

    val shape = remember { RoundedCornerShape(28.dp) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        contentAlignment = Alignment.Center
    ) {
        if (enabled || backgroundScale > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .graphicsLayer(
                        scaleX = backgroundScale,
                        scaleY = backgroundScale
                    )
                    .clip(shape)
                    .background(
                        color = colors.onBackground.copy(alpha = 0.08f),
                        shape = shape
                    )
                    .border(width = 1.dp, color = colors.outline, shape = shape)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(shape)
                .clickable(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        view.playSoundEffect(SoundEffectConstants.CLICK)

                        onClick()
                    },
                    indication = null,
                    interactionSource = interactionSource
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val iconModifier = Modifier.size(35.dp)

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

            if (enabled || checkmarkScale > 0.01f) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(28.dp)
                        .graphicsLayer(
                            scaleX = checkmarkScale, scaleY = checkmarkScale
                        ),
                    tint = colors.onBackground
                )
            }
        }
    }
}