package app.ninesevennine.twofactorauthenticator.ui.elements.wideradiobutton

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import app.ninesevennine.twofactorauthenticator.themeViewModel

@Composable
fun WideRadioButtonWithIcon(
    modifier: Modifier = Modifier,
    icon: Painter,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors

    WideRadioButtonWithIconInternal(
        modifier = modifier,
        iconContent = {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = it
            )
        },
        label = label,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun WideRadioButtonWithIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors

    WideRadioButtonWithIconInternal(
        modifier = modifier,
        iconContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = it
            )
        },
        label = label,
        enabled = enabled,
        onClick = onClick
    )
}