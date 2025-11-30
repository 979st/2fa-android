package app.ninesevennine.twofactorauthenticator.ui.elements.otpcard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.themeViewModel

@Composable
fun ClassicOtpCard(
    modifier: Modifier = Modifier,
    item: VaultItem,
    dragging: Boolean,
    enableEditing: Boolean = true
) {
    val context = LocalContext.current
    val theme = context.themeViewModel

    val colors = remember(item.otpCardColor) {
        theme.getOtpCardColors(context, item.otpCardColor)
    }

    val shape = RoundedCornerShape(32.dp)

    val cardModifier = modifier
        .padding(vertical = 8.dp, horizontal = 8.dp)
        .fillMaxWidth()
        .height(152.dp)
        .then(
            if (dragging) Modifier.shadow(
                elevation = 8.dp,
                shape = shape,
                clip = false
            ) else Modifier
        )
        .background(color = colors.firstColor, shape = shape)

    Box(
        contentAlignment = Alignment.Center,
        modifier = cardModifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize()
        ) {
            ClassicOtpCardUpper(item = item, enableEditing = enableEditing)
            ClassicOtpCardLower(item = item)
        }
    }
}