package app.ninesevennine.twofactorauthenticator.ui.elements.otpcard

import android.view.SoundEffectConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.features.otp.OtpTypes
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.EditScreenRoute
import app.ninesevennine.twofactorauthenticator.vaultViewModel
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun MinimalOtpCardUpper(
    item: VaultItem,
    enableEditing: Boolean
) {
    val context = LocalContext.current
    val theme = context.themeViewModel
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val navController = LocalNavController.current

    val vaultViewModel = context.vaultViewModel

    val colors = remember(item.otpCardColor) {
        theme.getOtpCardColors(context, item.otpCardColor)
    }

    val currentTimeSeconds by vaultViewModel.currentTimeSeconds.collectAsState()
    val secondsLeft = item.period - (currentTimeSeconds % item.period)
    val sweepAngle = secondsLeft.toFloat() / item.period.toFloat() * 360f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp)
            .height(42.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = item.issuer.ifEmpty { item.name },
                    fontFamily = InterVariable,
                    color = colors.thirdColor,
                    fontWeight = FontWeight.W700,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.name.isNotEmpty()) {
                    Text(
                        text = item.name,
                        fontFamily = InterVariable,
                        color = colors.thirdColor,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            if (item.otpType != OtpTypes.HOTP) {
                Canvas(
                    Modifier
                        .padding(end = 6.dp)
                        .size(20.dp)
                ) {
                    drawArc(
                        color = colors.secondColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            view.playSoundEffect(SoundEffectConstants.CLICK)

                            if (enableEditing) {
                                vaultViewModel.incrementItemCounter(item.uuid)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = colors.secondColor
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        view.playSoundEffect(SoundEffectConstants.CLICK)

                        if (enableEditing) {
                            navController.navigate(EditScreenRoute(item.uuid.toString()))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colors.secondColor
                )
            }
        }
    }
}