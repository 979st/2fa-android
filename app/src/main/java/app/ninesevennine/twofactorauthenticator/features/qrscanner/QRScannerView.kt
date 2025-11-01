package app.ninesevennine.twofactorauthenticator.features.qrscanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Size
import android.view.SoundEffectConstants
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.features.locale.localizedString
import app.ninesevennine.twofactorauthenticator.features.otp.otpParser
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.utils.Logger
import java.util.concurrent.Executors
import kotlin.math.min

@Composable
fun QRScannerView(
    onEnterManually: () -> Unit, onVaultItemChange: (VaultItem) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val navController = LocalNavController.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted })

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Surface(
        modifier = Modifier.fillMaxSize(), color = Color.Black
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                hasCameraPermission -> CameraPreview(onVaultItemChange)
                else -> PermissionRequiredMessage()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(all = navBottom + 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0x99000000))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                navController.popBackStack()
                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0x99000000))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                onEnterManually()
                            }
                            .padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = localizedString(R.string.qr_scanner_button_text_enter_manually),
                            fontFamily = InterVariable,
                            color = Color.White,
                            fontWeight = FontWeight.W700,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Add from image?
                    Spacer(Modifier.width(56.dp))
                }
            }
        }
    }
}

@Composable
private fun PermissionRequiredMessage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = localizedString(R.string.qr_scanner_text_camera_permission_required),
            fontFamily = InterVariable,
            textAlign = TextAlign.Center,
            color = Color.White,
            fontWeight = FontWeight.W700,
            fontSize = 20.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CameraPreview(
    onVaultItemChange: (VaultItem) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Use minimum screen dimension for viewfinder size
    @SuppressLint("ConfigurationScreenWidthHeight") val minScreenDp =
        min(configuration.screenWidthDp, configuration.screenHeightDp)
    val viewfinderPercent = 0.75f

    // Calculate analysis size as 75% of minimum screen dimension
    val analysisWidth = with(density) {
        (minScreenDp * viewfinderPercent).dp.toPx().toInt()
    }
    val analysisSize = Size(analysisWidth, analysisWidth)

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
        }
    }

    val imageAnalyzer = remember {
        ImageAnalysis.Builder().setResolutionSelector(
                ResolutionSelector.Builder().setResolutionStrategy(
                        ResolutionStrategy(
                            analysisSize, ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER
                        )
                    ).build()
            ).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setImageQueueDepth(1)
            .build()
    }

    val analyzer = remember {
        ZXingQrAnalyzer(viewfinderPercent) { scanned ->
            otpParser(scanned)?.let { item ->
                onVaultItemChange(item)
            }
        }
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(previewView, imageAnalyzer, lifecycleOwner) {
        cameraProviderFuture.addListener({
            runCatching {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    surfaceProvider = previewView.surfaceProvider
                }
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer
                )
            }.onFailure { e ->
                Logger.e("QRScannerView", "Camera bind failed: ${e.stackTraceToString()}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Set analyzer
    DisposableEffect(imageAnalyzer) {
        imageAnalyzer.setAnalyzer(cameraExecutor, analyzer)

        onDispose {
            cameraProviderFuture.addListener({
                runCatching {
                    cameraProviderFuture.get().unbindAll()
                }.onFailure { e ->
                    Logger.e("QRScannerView", "Camera unbind failed: ${e.stackTraceToString()}")
                }
            }, ContextCompat.getMainExecutor(context))

            imageAnalyzer.clearAnalyzer()
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { previewView }, modifier = Modifier.fillMaxSize()
        )

        QRScannerOverlay(
            modifier = Modifier.fillMaxSize(), viewfinderWidthPercent = viewfinderPercent
        )
    }
}