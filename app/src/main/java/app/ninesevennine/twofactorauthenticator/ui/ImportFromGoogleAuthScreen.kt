package app.ninesevennine.twofactorauthenticator.ui

import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.features.externalvault.GoogleAuthenticator
import app.ninesevennine.twofactorauthenticator.features.qrscanner.QRScannerView
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.features.vault.VaultItem
import app.ninesevennine.twofactorauthenticator.vaultViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object ImportFromGoogleAuthScreenRoute

@Composable
fun ImportFromGoogleAuthScreen() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val navController = LocalNavController.current
    val vaultViewModel = context.vaultViewModel

    var expectedBatchSize by remember { mutableIntStateOf(0) }
    var expectedBatchId by remember { mutableIntStateOf(0) }
    val importedBatches = remember { mutableStateMapOf<Int, List<VaultItem>>() }
    val scannedBatchIndexes = remember { mutableStateListOf<Int>() }
    val handledQRCodes = remember { mutableStateListOf<String>() }
    val coroutineScope = rememberCoroutineScope()

    QRScannerView(
        onQrCodeScanned = { rawQrContent ->
            if (handledQRCodes.contains(rawQrContent)) return@QRScannerView
            handledQRCodes.add(rawQrContent)

            coroutineScope.launch {
                val result = try {
                    GoogleAuthenticator.importFromQrCode(rawQrContent)
                } catch (_: Exception) {
                    null
                }

                if (result == null) {
                    handledQRCodes.remove(rawQrContent)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    return@launch
                }

                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.playSoundEffect(SoundEffectConstants.CLICK)

                val batchInfo = result.batchInfo
                if (batchInfo == null) {
                    vaultViewModel.restoreVaultItems(result.items)
                    navController.popBackStack(
                        navController.graph.startDestinationId,
                        inclusive = false
                    )
                    return@launch
                }

                if (expectedBatchId == 0) expectedBatchId = batchInfo.batchId
                if (expectedBatchSize == 0) expectedBatchSize = batchInfo.batchSize

                if (batchInfo.batchId != expectedBatchId) {
                    handledQRCodes.remove(rawQrContent)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    return@launch
                }

                val idx = batchInfo.batchIndex
                if (!importedBatches.containsKey(idx)) {
                    importedBatches[idx] = result.items
                    scannedBatchIndexes.add(idx)
                }

                if (importedBatches.size == expectedBatchSize) {
                    val allItems = (0 until expectedBatchSize).flatMap { i ->
                        importedBatches[i] ?: emptyList()
                    }
                    vaultViewModel.restoreVaultItems(allItems)
                    navController.popBackStack(
                        navController.graph.startDestinationId,
                        inclusive = false
                    )
                }
            }
        },
        bottomBar = {
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

                if (expectedBatchSize > 1) {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(Color(0x99000000))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                view.playSoundEffect(SoundEffectConstants.CLICK)

                            }
                            .padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "${importedBatches.size} / $expectedBatchSize",
                            fontFamily = InterVariable,
                            color = Color.White,
                            fontWeight = FontWeight.W700,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }
                } else {
                    Spacer(Modifier.weight(1f))
                }

                Spacer(Modifier.weight(1f))

                Spacer(Modifier.width(56.dp))
            }
        }
    )
}