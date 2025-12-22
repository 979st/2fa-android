package app.ninesevennine.twofactorauthenticator.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.features.locale.localizedString
import app.ninesevennine.twofactorauthenticator.features.theme.InterVariable
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedButton
import app.ninesevennine.twofactorauthenticator.ui.elements.SectionButton
import app.ninesevennine.twofactorauthenticator.ui.elements.SectionConfidentialTextBox
import app.ninesevennine.twofactorauthenticator.ui.elements.SectionGroup
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

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                restoreFilename = getDocumentName(context, uri) ?: "???"

                if (!restoreFilename.endsWith(".2fa", ignoreCase = true)) {
                    Logger.e("RestoreVaultScreen", "Invalid file type. Please select a .2fa file")

                    navController.popBackStack()
                    return@rememberLauncherForActivityResult
                }

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    restoreContent =
                        inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

                    Logger.i("RestoreVaultScreen", "Vault successfully read")
                }
            } catch (e: Exception) {
                Logger.e("RestoreVaultScreen", "Error reading vault: ${e.message}")
            }
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        openDocumentLauncher.launch(arrayOf("application/octet-stream"))
    }

    if (restoreContent.isEmpty()) return

    val spinnerFrames = arrayOf("|", "/", "-", "\\")
    var spinnerIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRestoring) {
        while (isRestoring) {
            spinnerIndex = (spinnerIndex + 1) % spinnerFrames.size
            delay(200L)
        }
    }

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val bottomPadding = if (imeBottom > 0.dp) imeBottom else navBottom

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxHeight()
                .padding(
                    start = navBottom,
                    bottom = navBottom,
                    end = navBottom,
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.padding(bottom = bottomPadding)) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

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

                SectionGroup(
                    modifier = Modifier.padding(vertical = 16.dp),
                    title = localizedString(R.string.restore_vault_section_credentials_title)
                ) {
                    SectionButton(
                        imageVector = Icons.Default.Description,
                        primaryText = restoreFilename
                    )
                    SectionConfidentialTextBox(
                        title = localizedString(R.string.restore_vault_section_credentials_textbox_password),
                        value = password,
                        onValueChange = { password = it },
                        error = password.isEmpty()
                    )
                    if (restoreError) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = localizedString(R.string.restore_vault_section_credentials_incorrect_password),
                                modifier = Modifier.padding(start = 4.dp),
                                color = colors.error,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.W700,
                                fontFamily = InterVariable
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                RoundedButton(
                    label = if (isRestoring)
                        spinnerFrames[spinnerIndex]
                    else
                        localizedString(R.string.restore_vault_button_restore)
                ) {
                    if (isRestoring || password.isEmpty()) {
                        return@RoundedButton
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
            }

            RoundedButton(localizedString(R.string.restore_vault_button_go_back)) {
                navController.popBackStack()
            }
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