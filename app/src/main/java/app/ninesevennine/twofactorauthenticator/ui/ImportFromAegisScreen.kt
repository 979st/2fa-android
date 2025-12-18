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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.features.externalvault.AegisAuthenticator
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
object ImportFromAegisScreenRoute

@Composable
fun ImportFromAegisScreen() {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    val navController = LocalNavController.current
    val vaultViewModel = context.vaultViewModel

    var password by remember { mutableStateOf("") }

    val importScope = rememberCoroutineScope()
    var importContent by remember { mutableStateOf("") }
    var importFilename by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf(false) }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                importFilename = getDocumentName(context, uri) ?: "???"

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val vault = AegisAuthenticator.importUnencrypted(content)

                    if (vault == null) {
                        importContent = content
                        Logger.i(
                            "ImportFromAegisScreen",
                            "Password required to import from this Aegis vault"
                        )
                    } else {
                        vaultViewModel.restoreVaultItems(vault)

                        navController.popBackStack(
                            navController.graph.startDestinationId,
                            inclusive = false
                        )

                        Logger.i("ImportFromAegisScreen", "Aegis successfully read")
                    }
                }
            } catch (e: Exception) {
                Logger.e("ImportFromAegisScreen", "Error reading Aegis: ${e.message}")
            }
        } else {
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        openDocumentLauncher.launch(arrayOf("application/json"))
    }

    if (importContent.isEmpty()) return

    val spinnerFrames = arrayOf("|", "/", "-", "\\")
    var spinnerIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isImporting) {
        while (isImporting) {
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
                        painter = painterResource(R.drawable.aegis),
                        contentDescription = null,
                        modifier = Modifier.size(128.dp),
                        tint = colors.onBackground
                    )
                }

                SectionGroup(
                    modifier = Modifier.padding(vertical = 16.dp),
                    title = "Credentials"
                ) {
                    SectionButton(
                        imageVector = Icons.Default.Description,
                        primaryText = importFilename
                    )
                    SectionConfidentialTextBox(
                        title = "Password",
                        value = password,
                        onValueChange = { password = it },
                        error = password.isEmpty()
                    )
                    if (importError) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Incorrect password",
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
                    label = if (isImporting)
                        spinnerFrames[spinnerIndex]
                    else
                        "Import"
                ) {
                    if (isImporting || password.isEmpty()) {
                        return@RoundedButton
                    }

                    isImporting = true

                    importScope.launch {
                        val success = withContext(Dispatchers.Default) {
                            val vault =
                                AegisAuthenticator.importEncrypted(importContent, password)
                            if (vault != null) {
                                vaultViewModel.restoreVaultItems(vault)
                            }
                            vault != null
                        }

                        if (success) {
                            navController.popBackStack(
                                navController.graph.startDestinationId,
                                inclusive = false
                            )
                        } else {
                            importError = true
                            isImporting = false
                        }
                    }
                }
            }

            RoundedButton("Cancel") {
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