package app.ninesevennine.twofactorauthenticator.ui.elements.widebutton

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun WideButtonWithTintedIcon(
    modifier: Modifier = Modifier,
    icon: Painter,
    tint: Color,
    label: String,
    onClick: () -> Unit
) {
    WideButtonWithIconInternal(
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
        onClick = onClick
    )
}

@Composable
fun WideButtonWithTintedIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    tint: Color,
    label: String,
    onClick: () -> Unit
) {
    WideButtonWithIconInternal(
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
        onClick = onClick
    )
}