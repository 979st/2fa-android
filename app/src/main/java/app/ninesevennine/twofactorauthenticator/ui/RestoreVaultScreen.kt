package app.ninesevennine.twofactorauthenticator.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.features.locale.localizedString
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.WideText
import app.ninesevennine.twofactorauthenticator.ui.elements.WideTitle
import app.ninesevennine.twofactorauthenticator.ui.elements.textfields.ConfidentialSingleLineTextField
import app.ninesevennine.twofactorauthenticator.ui.elements.widebutton.WideButton
import app.ninesevennine.twofactorauthenticator.utils.Logger
import app.ninesevennine.twofactorauthenticator.vaultViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
object RestoreVaultScreenRoute

@Composable
fun RestoreVaultScreen() {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    val navController = LocalNavController.current
    val vaultViewModel = context.vaultViewModel

    var password by remember { mutableStateOf("") }

    val restoreScope = rememberCoroutineScope()
    var restoreContent by remember { mutableStateOf("") }
    var restoreFilename by remember { mutableStateOf("") }
    var isRestoring by remember { mutableStateOf(false) }
    var restoreError by remember { mutableStateOf(false) }

    val dots = arrayOf("", ".", "..", "...")
    var dotCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRestoring) {
        while (isRestoring) {
            dotCount = (dotCount + 1) % 4
            delay(250L)
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                restoreFilename = getDocumentName(context, uri) ?: "???"

                if (!restoreFilename.endsWith(".2fa", ignoreCase = true)) {
                    Logger.e("BackupVaultScreen", "Invalid file type. Please select a .2fa file")

                    navController.popBackStack()
                    return@rememberLauncherForActivityResult
                }

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    restoreContent =
                        inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

                    Logger.i("BackupVaultScreen", "Vault successfully read")
                }
            } catch (e: Exception) {
                Logger.e("BackupVaultScreen", "Error reading vault: ${e.message}")
            }
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        openDocumentLauncher.launch(arrayOf("application/octet-stream"))
    }

    if (restoreContent.isEmpty()) return

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
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
        ) {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Download,
                        contentDescription = null,
                        modifier = Modifier.size(128.dp),
                        tint = colors.onBackground
                    )
                }

                WideTitle(text = "${localizedString(R.string.restore_prompt_credentials)} ($restoreFilename)")

                ConfidentialSingleLineTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { password = it },
                    placeholder = localizedString(R.string.common_password_hint),
                    isError = password.isEmpty()
                )

                if (restoreError) WideText(
                    text = localizedString(R.string.restore_error_incorrect_password),
                    color = colors.error
                )

                WideButton(
                    label = if (isRestoring)
                        "${localizedString(R.string.restore_status_restoring)}${dots[dotCount]}"
                    else
                        localizedString(R.string.restore_button_action),
                    color = colors.primary,
                    textColor = colors.onPrimary,
                    onClick = {
                        if (isRestoring || password.isEmpty()) {
                            return@WideButton
                        }

                        isRestoring = true

                        restoreScope.launch {
                            val success = withContext(Dispatchers.Default) {
                                vaultViewModel.restoreVault(password, restoreContent)
                            }

                            if (success) {
                                navController.popBackStack(
                                    navController.graph.startDestinationId,
                                    inclusive = false
                                )
                            } else {
                                restoreError = true
                                isRestoring = false
                            }
                        }
                    }
                )
            }

            WideButton(
                label = localizedString(R.string.common_cancel),
                onClick = { navController.popBackStack() }
            )
        }
    }
}

private fun getDocumentName(context: Context, uri: Uri): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                return cursor.getString(nameIndex)
            }
        }
    }
    return null
}