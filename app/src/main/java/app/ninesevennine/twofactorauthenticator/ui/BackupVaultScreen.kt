package app.ninesevennine.twofactorauthenticator.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
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
import app.ninesevennine.twofactorauthenticator.utils.Password
import app.ninesevennine.twofactorauthenticator.vaultViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
object BackupVaultScreenRoute

@Composable
fun BackupVaultScreen() {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    val navController = LocalNavController.current
    val vaultViewModel = context.vaultViewModel

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordLong by remember { mutableStateOf(false) }
    var hasPasswordUppercase by remember { mutableStateOf(false) }
    var hasPasswordDigit by remember { mutableStateOf(false) }
    var hasPasswordSpecial by remember { mutableStateOf(false) }

    isPasswordLong = Password.isLong(password)
    hasPasswordUppercase = Password.hasUppercase(password)
    hasPasswordDigit = Password.hasDigit(password)
    hasPasswordSpecial = Password.hasSpecial(password)

    val isPasswordStrong =
        isPasswordLong && hasPasswordUppercase && hasPasswordDigit && hasPasswordSpecial

    var passwordsMatch by remember { mutableStateOf(true) }
    passwordsMatch = password == confirmPassword

    val backupScope = rememberCoroutineScope()
    var backupContent by remember { mutableStateOf("") }
    var isBackingUp by remember { mutableStateOf(false) }

    val dots = arrayOf("", ".", "..", "...")
    var dotCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(isBackingUp) {
        while (isBackingUp) {
            dotCount = (dotCount + 1) % 4
            delay(250L)
        }
    }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = CreateDocument("application/octet-stream"),
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(backupContent.toByteArray())
                }

                Logger.i("BackupVaultScreen", "Vault successfully saved")
                navController.popBackStack(
                    navController.graph.startDestinationId,
                    inclusive = false
                )
            } catch (e: Exception) {
                Logger.e("BackupVaultScreen", "Error saving vault: ${e.message}")
            }
        }

        isBackingUp = false
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
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start,
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = bottomPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(128.dp),
                        tint = colors.onBackground
                    )
                }

                WideTitle(text = localizedString(R.string.backup_title_credentials))

                ConfidentialSingleLineTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = password,
                    onValueChange = { password = it },
                    placeholder = localizedString(R.string.common_password_hint),
                    isError = !isPasswordStrong
                )

                if (!isPasswordLong) WideText(
                    text = localizedString(R.string.backup_error_password_length),
                    color = colors.error
                )

                if (!hasPasswordUppercase) WideText(
                    text = localizedString(R.string.backup_error_password_uppercase),
                    color = colors.error
                )

                if (!hasPasswordDigit) WideText(
                    text = localizedString(R.string.backup_error_password_digit),
                    color = colors.error
                )

                if (!hasPasswordSpecial) WideText(
                    text = localizedString(R.string.backup_error_password_special),
                    color = colors.error
                )

                ConfidentialSingleLineTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = localizedString(R.string.backup_field_confirm_password_hint),
                    isError = !passwordsMatch
                )

                WideButton(
                    label = if (isBackingUp)
                        "${localizedString(R.string.backup_status_backing_up)}${dots[dotCount]}"
                    else
                        localizedString(R.string.backup_button_action),
                    color = colors.primary,
                    textColor = colors.onPrimary,
                    onClick = {
                        if (!isPasswordStrong || !passwordsMatch || isBackingUp) {
                            return@WideButton
                        }

                        isBackingUp = true

                        backupScope.launch {
                            backupContent = withContext(Dispatchers.Default) {
                                vaultViewModel.backupVault(password)
                            }

                            if (backupContent.isEmpty()) {
                                Logger.e("BackupVaultScreen", "Backup content is empty")
                                isBackingUp = false
                                return@launch
                            }

                            createDocumentLauncher.launch("backup.2fa")
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