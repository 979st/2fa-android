package app.ninesevennine.twofactorauthenticator.ui.elements.otpcard

import android.view.SoundEffectConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.LocalThemeViewModel
import app.ninesevennine.twofactorauthenticator.LocalVaultViewModel
import app.ninesevennine.twofactorauthenticator.features.otp.OtpIssuer
import app.ninesevennine.twofactorauthenticator.features.otp.OtpTypes
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.ui.EditScreenRoute
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun OtpCardUpper(
    item: VaultItem,
    enableEditing: Boolean
) {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    val theme = LocalThemeViewModel.current
    val colors = remember(item.otpCardColor) {
        theme.getOtpCardColors(item.otpCardColor)
    }
    val navController = LocalNavController.current
    val vaultViewModel = LocalVaultViewModel.current

    val issuerIcon = remember(item.issuer) { OtpIssuer.getIcon(item.issuer) }

    val currentTimeSeconds by vaultViewModel.currentTimeSeconds.collectAsState()
    val secondsLeft = item.period - (currentTimeSeconds % item.period)
    val sweepAngle = secondsLeft.toFloat() / item.period.toFloat() * 360f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                if (issuerIcon != null) {
                    Icon(
                        painter = painterResource(id = issuerIcon),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Unspecified
                    )
                } else {
                    Canvas(Modifier.size(64.dp)) {
                        drawCircle(
                            color = colors.thirdColor
                        )
                    }

                    Text(
                        text = if (item.issuer.isNotEmpty()) {
                            item.issuer.first().uppercase()
                        } else if (item.name.isNotEmpty()) {
                            item.name.first().uppercase()
                        } else {
                            "?"
                        },
                        fontFamily = InterVariable,
                        color = colors.firstColor,
                        fontWeight = FontWeight.W700,
                        fontSize = 32.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

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
                    fontSize = 18.sp,
                    maxLines = 1
                )

                if (item.name.isNotEmpty()) {
                    Text(
                        text = item.name,
                        fontFamily = InterVariable,
                        color = colors.thirdColor,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            if (item.otpType != OtpTypes.HOTP) {
                Canvas(Modifier.size(32.dp)) {
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
                        .size(48.dp)
                        .background(
                            color = colors.secondColor,
                            shape = RoundedCornerShape(8.dp)
                        )
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
                        tint = colors.firstColor
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = colors.secondColor,
                        shape = RoundedCornerShape(8.dp)
                    )
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
                    tint = colors.firstColor
                )
            }
        }
    }
}