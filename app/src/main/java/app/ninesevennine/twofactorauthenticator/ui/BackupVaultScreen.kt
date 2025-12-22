package app.ninesevennine.twofactorauthenticator.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
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
import app.ninesevennine.twofactorauthenticator.ui.elements.SectionConfidentialTextBox
import app.ninesevennine.twofactorauthenticator.ui.elements.SectionGroup
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
    var hasNoLeadingOrTrailingWhitespace by remember { mutableStateOf(false) }
    var hasPasswordDigit by remember { mutableStateOf(false) }
    var hasPasswordSpecial by remember { mutableStateOf(false) }

    isPasswordLong = Password.isLong(password)
    hasNoLeadingOrTrailingWhitespace = Password.hasNoLeadingOrTrailingWhitespace(password)
    hasPasswordDigit = Password.hasDigit(password)
    hasPasswordSpecial = Password.hasSpecial(password)

    val isPasswordStrong = Password.isValid(password)

    var passwordsMatch by remember { mutableStateOf(true) }
    passwordsMatch = password == confirmPassword

    val backupScope = rememberCoroutineScope()
    var backupContent by remember { mutableStateOf("") }
    var isBackingUp by remember { mutableStateOf(false) }

    val spinnerFrames = arrayOf("|", "/", "-", "\\")
    var spinnerIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isBackingUp) {
        while (isBackingUp) {
            spinnerIndex = (spinnerIndex + 1) % spinnerFrames.size
            delay(200L)
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
                .verticalScroll(rememberScrollState())
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
                        imageVector = Icons.Filled.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(128.dp),
                        tint = colors.onBackground
                    )
                }

                SectionGroup(
                    modifier = Modifier.padding(vertical = 16.dp),
                    title = localizedString(R.string.backup_vault_section_credentials_title)
                ) {
                    Spacer(Modifier.height(6.dp))
                    SectionConfidentialTextBox(
                        title = localizedString(R.string.backup_vault_section_credentials_textbox_password),
                        value = password,
                        onValueChange = { password = it },
                        error = !isPasswordStrong
                    )
                    SectionConfidentialTextBox(
                        title = localizedString(R.string.backup_vault_section_credentials_textbox_confirm_password),
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        error = !passwordsMatch
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isPasswordStrong)
                                localizedString(R.string.backup_vault_section_credentials_requirements_met)
                            else
                                localizedString(R.string.backup_vault_section_credentials_requirements_not_met),
                            modifier = Modifier.padding(start = 4.dp),
                            color = colors.onBackground,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.W700,
                            fontFamily = InterVariable
                        )
                        Text(
                            text = localizedString(R.string.backup_vault_section_credentials_requirement_8_chars),
                            modifier = Modifier.padding(start = 20.dp),
                            color = if (isPasswordLong) colors.onBackground else colors.error,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.W700,
                            fontFamily = InterVariable,
                            maxLines = 1
                        )
                        Text(
                            text = localizedString(R.string.backup_vault_section_credentials_requirement_1_num),
                            modifier = Modifier.padding(start = 20.dp),
                            color = if (hasPasswordDigit) colors.onBackground else colors.error,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.W700,
                            fontFamily = InterVariable,
                            maxLines = 1
                        )
                        Text(
                            text = localizedString(R.string.backup_vault_section_credentials_requirement_1_special),
                            modifier = Modifier.padding(start = 20.dp),
                            color = if (hasPasswordSpecial) colors.onBackground else colors.error,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.W700,
                            fontFamily = InterVariable,
                            maxLines = 1
                        )
                        Text(
                            text = localizedString(R.string.backup_vault_section_credentials_requirement_trim),
                            modifier = Modifier.padding(start = 20.dp),
                            color = if (hasNoLeadingOrTrailingWhitespace) colors.onBackground else colors.error,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.W700,
                            fontFamily = InterVariable,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                RoundedButton(
                    label = if (isBackingUp)
                        spinnerFrames[spinnerIndex]
                    else
                        localizedString(R.string.backup_vault_button_create_backup)
                ) {
                    if (!isPasswordStrong || !passwordsMatch || isBackingUp) {
                        return@RoundedButton
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

                Spacer(modifier = Modifier.height(16.dp))
            }

            RoundedButton(localizedString(R.string.backup_vault_button_go_back)) {
                navController.popBackStack()
            }
        }
    }
}