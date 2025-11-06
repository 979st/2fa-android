package app.ninesevennine.twofactorauthenticator.ui.elements.wideradiobutton

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun WideRadioButtonWithTintedIcon(
    modifier: Modifier = Modifier,
    icon: Painter,
    tint: Color,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    WideRadioButtonWithIconInternal(
        modifier = modifier,
        iconContent = {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = tint,
                modifier = it
            )
        },
        label = label,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun WideRadioButtonWithTintedIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    WideRadioButtonWithIconInternal(
        modifier = modifier,
        iconContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = it
            )
        },
        label = label,
        enabled = enabled,
        onClick = onClick
    )
}