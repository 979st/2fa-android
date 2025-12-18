package app.ninesevennine.twofactorauthenticator.ui

import android.view.SoundEffectConstants
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedButton
import app.ninesevennine.twofactorauthenticator.vaultViewModel
import kotlinx.serialization.Serializable

@Serializable
object ExportToGoogleAuthScreenRoute

@Composable
fun ExportToGoogleAuthScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val colors = context.themeViewModel.colors
    val navController = LocalNavController.current
    val vaultViewModel = context.vaultViewModel

    BrightnessController(setBrightness = true, brightnessValue = 1.0f)

    val qrBitmaps by remember { mutableStateOf(vaultViewModel.exportToGoogleAuth()) }
    val qrPages by remember { mutableIntStateOf(qrBitmaps.size) }
    var qrIndex by remember { mutableIntStateOf(0) }

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val shape = remember { RoundedCornerShape(28.dp) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxHeight()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = navPadding
                )
                .padding(horizontal = navPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Icon(
                    painter = painterResource(R.drawable.icon_google_authenticator),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp),
                    tint = Color.Unspecified
                )

                if (qrBitmaps.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .padding(all = 8.dp)
                                .widthIn(max = 400.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        ) {
                            drawRect(
                                color = colors.error,
                                topLeft = Offset(0f, 0f),
                                size = Size(size.width, size.width)
                            )
                        }

                        Text(
                            text = "?",
                            fontFamily = InterVariable,
                            color = colors.onError,
                            fontWeight = FontWeight.W700,
                            fontSize = 64.sp,
                        )
                    }
                } else {
                    Image(
                        painter = BitmapPainter(
                            qrBitmaps[qrIndex].asImageBitmap(),
                            filterQuality = FilterQuality.None
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .widthIn(max = 400.dp)
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }

                Text(
                    text = "Only TOTP and HOTP tokens that generate 6-digit or 8-digit codes with 30 second intervals can be exported to Google Authenticator",
                    color = colors.onBackground,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.W700,
                    fontFamily = InterVariable,
                    textAlign = TextAlign.Center
                )

                if (qrBitmaps.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(shape)
                                .background(
                                    color = colors.background,
                                    shape = shape
                                )
                                .border(width = 1.dp, color = colors.outline, shape = shape)
                                .clickable(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        view.playSoundEffect(SoundEffectConstants.CLICK)

                                        qrIndex = (qrIndex - 1).coerceAtLeast(0)
                                    },
                                    indication = ripple(color = colors.onBackground.copy(alpha = 0.08f)),
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier.size(35.dp)
                            )
                        }

                        Text(
                            text = "${qrIndex + 1} / $qrPages",
                            color = colors.onBackground,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.W700,
                            fontFamily = InterVariable,
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(shape)
                                .background(
                                    color = colors.background,
                                    shape = shape
                                )
                                .border(width = 1.dp, color = colors.outline, shape = shape)
                                .clickable(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        view.playSoundEffect(SoundEffectConstants.CLICK)

                                        qrIndex = (qrIndex + 1).coerceAtMost(qrPages - 1)
                                    },
                                    indication = ripple(color = colors.onBackground.copy(alpha = 0.08f)),
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier.size(35.dp)
                            )
                        }
                    }
                }
            }

            RoundedButton("Done") {
                navController.popBackStack()
            }
        }
    }
}

@Composable
fun BrightnessController(
    setBrightness: Boolean,
    brightnessValue: Float = 1.0f
) {
    val view = LocalView.current

    DisposableEffect(setBrightness) {
        val window = (view.context as ComponentActivity).window
        val layoutParams = window.attributes

        if (setBrightness) {
            layoutParams.screenBrightness = brightnessValue
            window.attributes = layoutParams
        }

        onDispose {
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.attributes = layoutParams
        }
    }
}