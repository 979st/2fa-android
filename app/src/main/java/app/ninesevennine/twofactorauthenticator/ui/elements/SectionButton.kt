package app.ninesevennine.twofactorauthenticator.ui.elements

import android.view.SoundEffectConstants
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
fun SectionButton(
    painter: Painter? = null,
    imageVector: ImageVector? = null,
    primaryText: String,
    secondaryText: String? = null,
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
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "checkmarkScale"
    )

    var pressed by remember { mutableStateOf(false) }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(100)
            onClick()
            pressed = false
        }
    }

    val contentScale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "contentScale"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    view.playSoundEffect(SoundEffectConstants.CLICK)

                    if (!pressed) {
                        pressed = true
                    }
                },
                indication = ripple(color = colors.onBackground.copy(alpha = 0.08f)),
                interactionSource = interactionSource
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val iconModifier = Modifier
            .size(40.dp)
            .graphicsLayer(
                scaleX = contentScale,
                scaleY = contentScale
            )

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

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = primaryText,
                color = colors.onBackground,
                fontSize = 14.sp,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.W700,
                fontFamily = InterVariable,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )

            secondaryText?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    color = colors.onBackground,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.W500,
                    fontFamily = InterVariable,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }

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