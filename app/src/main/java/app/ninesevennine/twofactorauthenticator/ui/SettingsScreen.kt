package app.ninesevennine.twofactorauthenticator.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.configViewModel
import app.ninesevennine.twofactorauthenticator.features.theme.ThemeOption
import app.ninesevennine.twofactorauthenticator.localeViewModel
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.SectionButton
import app.ninesevennine.twofactorauthenticator.ui.elements.SectionGroup
import app.ninesevennine.twofactorauthenticator.ui.elements.bottomappbar.SettingsAppBar
import app.ninesevennine.twofactorauthenticator.utils.Logger
import kotlinx.serialization.Serializable

@Serializable
object SettingsScreenRoute

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val colors = context.themeViewModel.colors
    val navController = LocalNavController.current
    val themeViewModel = context.themeViewModel
    val configViewModel = context.configViewModel
    val localeViewModel = context.localeViewModel

    val internalAppLogLauncher = rememberLauncherForActivityResult(
        contract = CreateDocument("text/plain"),
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val logContent = Logger.getFullLog()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(logContent.toByteArray())
                }
            } catch (e: Exception) {
                Logger.e("internalAppLogLauncher", "Error saving log file: ${e.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            SectionGroup(
                modifier = Modifier.padding(all = 16.dp),
                title = "Appearance"
            ) {
                SectionButton(
                    imageVector = Icons.Default.Language,
                    primaryText = "Language",
                    secondaryText = languageLabelFromLocale(localeViewModel.effectiveLocale),
                    onClick = { navController.navigate(LanguageSelectionScreenRoute) }
                )
                SectionButton(
                    imageVector = when (themeViewModel.theme) {
                        ThemeOption.LIGHT.value -> Icons.Default.LightMode
                        ThemeOption.DARK.value -> Icons.Default.DarkMode
                        else -> null
                    },
                    primaryText = "Theme",
                    secondaryText = if (themeViewModel.theme == ThemeOption.SYSTEM_DEFAULT.value) {
                        if (themeViewModel.isSystemDark(context)) {
                            "Dark"
                        } else {
                            "Light"
                        }
                    } else {
                        when (themeViewModel.theme) {
                            ThemeOption.LIGHT.value -> "Light"
                            ThemeOption.DARK.value -> "Dark"
                            else -> null
                        }
                    },
                    onClick = { navController.navigate(ThemeSelectionScreenRoute) }
                )
                SectionButton(
                    imageVector = Icons.Default.Star,
                    primaryText = "Card style",
                    secondaryText = when (configViewModel.values.cardStyle) {
                        0 -> "Classic"
                        1 -> "Minimal"
                        else -> "Unknown"
                    },
                    onClick = { navController.navigate(CardStyleSelectionScreenRoute)}
                )

                val requireTapToReveal = context.configViewModel.values.requireTapToReveal
                SectionButton(
                    imageVector = Icons.Default.TouchApp,
                    primaryText = "Tap to reveal codes",
                    enabled = requireTapToReveal,
                    onClick = { context.configViewModel.updateTapToReveal(!requireTapToReveal) }
                )
            }

            SectionGroup(
                modifier = Modifier.padding(all = 16.dp),
                title = "Behavior"
            ) {
                val enableFocusSearch = context.configViewModel.values.enableFocusSearch
                SectionButton(
                    painter = painterResource(R.drawable.frame_inspect),
                    tint = colors.onBackground,
                    primaryText = "Focus search on launch",
                    enabled = enableFocusSearch,
                    onClick = { context.configViewModel.updateFocusSearch(!enableFocusSearch) }
                )
            }

            SectionGroup(
                modifier = Modifier.padding(all = 16.dp),
                title = "Backup & Restore"
            ) {
                SectionButton(
                    imageVector = Icons.Default.Upload,
                    primaryText = "Backup codes",
                    onClick = { navController.navigate(BackupVaultScreenRoute) }
                )

                SectionButton(
                    imageVector = Icons.Default.Download,
                    primaryText = "Restore codes",
                    onClick = { navController.navigate(RestoreVaultScreenRoute) }
                )

                SectionButton(
                    painter = painterResource(R.drawable.icon_google_authenticator),
                    primaryText = "Export to Google Authenticator",
                    secondaryText = "QR code",
                    onClick = { navController.navigate(ExportToGoogleAuthScreenRoute) }
                )

                SectionButton(
                    painter = painterResource(R.drawable.icon_google_authenticator),
                    primaryText = "Import from Google Authenticator",
                    secondaryText = "QR code",
                    onClick = { navController.navigate(ImportFromGoogleAuthScreenRoute) }
                )

                SectionButton(
                    painter = painterResource(R.drawable.aegis),
                    tint = colors.onBackground,
                    primaryText = "Import from Aegis",
                    secondaryText = "JSON file",
                    onClick = { navController.navigate(ImportFromAegisScreenRoute) }
                )
            }

            SectionGroup(
                modifier = Modifier.padding(all = 16.dp),
                title = "Security"
            ) {
                SectionButton(
                    imageVector = Icons.Default.Description,
                    primaryText = "Download internal app log",
                    onClick = {
                        Logger.i("SettingsScreen", "Downloading internal app log")
                        internalAppLogLauncher.launch("2fa_log")
                    }
                )

                val screenSecurity = context.configViewModel.values.screenSecurity
                SectionButton(
                    imageVector = Icons.Default.ScreenLockPortrait,
                    primaryText = "Screen security",
                    secondaryText = "Block screenshots and recordings",
                    enabled = screenSecurity,
                    onClick = { context.configViewModel.updateScreenSecurity(!screenSecurity) }
                )

                val antiPixnapping = context.configViewModel.values.antiPixnapping
                SectionButton(
                    imageVector = Icons.Default.BugReport,
                    tint = Color.Red,
                    primaryText = "Anti-Pixnapping",
                    secondaryText = "CVE-2025-48561",
                    enabled = antiPixnapping,
                    onClick = { context.configViewModel.updateAntiPixnapping(!antiPixnapping) }
                )
            }

            SectionGroup(
                modifier = Modifier.padding(all = 16.dp),
                title = "About"
            ) {
                SectionButton(
                    primaryText = "979",
                    secondaryText = "About us",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://979.st/en/about".toUri())
                        context.startActivity(intent)
                    }
                )
                SectionButton(
                    painter = painterResource(R.drawable.github),
                    tint = colors.onBackground,
                    primaryText = "Source code",
                    secondaryText = "View 2fa's source code",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/979st/2fa-android".toUri())
                        context.startActivity(intent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(192.dp))
        }
    }

    SettingsAppBar()
}
