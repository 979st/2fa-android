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
import app.ninesevennine.twofactorauthenticator.features.locale.localizedString
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
                title = localizedString(R.string.settings_section_appearance_title)
            ) {
                SectionButton(
                    imageVector = Icons.Default.Language,
                    primaryText = localizedString(R.string.settings_section_appearance_button_language_primary),
                    secondaryText = languageLabelFromLocale(localeViewModel.effectiveLocale),
                    onClick = { navController.navigate(LanguageSelectionScreenRoute) }
                )

                SectionButton(
                    imageVector = when (themeViewModel.theme) {
                        ThemeOption.LIGHT.value -> Icons.Default.LightMode
                        ThemeOption.DARK.value -> Icons.Default.DarkMode
                        else -> null
                    },
                    primaryText = localizedString(R.string.settings_section_appearance_button_theme_primary),
                    secondaryText = if (themeViewModel.theme == ThemeOption.SYSTEM_DEFAULT.value) {
                        if (themeViewModel.isSystemDark(context)) {
                            localizedString(R.string.theme_option_dark)
                        } else {
                            localizedString(R.string.theme_option_light)
                        }
                    } else {
                        when (themeViewModel.theme) {
                            ThemeOption.LIGHT.value -> localizedString(R.string.theme_option_light)
                            ThemeOption.DARK.value -> localizedString(R.string.theme_option_dark)
                            else -> null
                        }
                    },
                    onClick = { navController.navigate(ThemeSelectionScreenRoute) }
                )

                SectionButton(
                    imageVector = Icons.Default.Star,
                    primaryText = localizedString(R.string.settings_section_appearance_button_card_style_primary),
                    secondaryText = when (configViewModel.values.cardStyle) {
                        0 -> localizedString(R.string.card_style_option_classic)
                        1 -> localizedString(R.string.card_style_option_minimalist)
                        else -> "Unknown"
                    },
                    onClick = { navController.navigate(CardStyleSelectionScreenRoute)}
                )
            }

            SectionGroup(
                modifier = Modifier.padding(all = 16.dp),
                title = localizedString(R.string.settings_section_behavior_title)
            ) {
                val requireTapToReveal = context.configViewModel.values.requireTapToReveal
                SectionButton(
                    imageVector = Icons.Default.TouchApp,
                    primaryText = localizedString(R.string.settings_section_behavior_button_tap_to_reveal_primary),
                    enabled = requireTapToReveal,
                    onClick = { context.configViewModel.updateTapToReveal(!requireTapToReveal) }
                )

                val enableFocusSearch = context.configViewModel.values.enableFocusSearch
                SectionButton(
                    painter = painterResource(R.drawable.frame_inspect),
                    tint = colors.onBackground,
                    primaryText = localizedString(R.string.settings_section_behavior_button_focus_search_primary),
                    enabled = enableFocusSearch,
                    onClick = { context.configViewModel.updateFocusSearch(!enableFocusSearch) }
                )
            }

            SectionGroup(
                modifier = Modifier.padding(all = 16.dp),
                title = localizedString(R.string.settings_section_security_title)
            ) {
                val screenSecurity = context.configViewModel.values.screenSecurity
                SectionButton(
                    imageVector = Icons.Default.ScreenLockPortrait,
                    primaryText = localizedString(R.string.settings_section_security_button_screen_security_primary),
                    secondaryText = localizedString(R.string.settings_section_security_button_screen_security_secondary),
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
                title = localizedString(R.string.settings_section_backup_and_restore_title)
            ) {
                SectionButton(
                    imageVector = Icons.Default.Upload,
                    primaryText = localizedString(R.string.settings_section_backup_and_restore_button_backup_accounts_primary),
                    onClick = { navController.navigate(BackupVaultScreenRoute) }
                )

                SectionButton(
                    imageVector = Icons.Default.Download,
                    primaryText = localizedString(R.string.settings_section_backup_and_restore_button_restore_accounts_primary),
                    onClick = { navController.navigate(RestoreVaultScreenRoute) }
                )

                SectionButton(
                    painter = painterResource(R.drawable.icon_google_authenticator),
                    primaryText = localizedString(R.string.settings_section_backup_and_restore_button_export_google_primary),
                    secondaryText = localizedString(R.string.settings_section_backup_and_restore_button_export_google_secondary),
                    onClick = { navController.navigate(ExportToGoogleAuthScreenRoute) }
                )

                SectionButton(
                    painter = painterResource(R.drawable.icon_google_authenticator),
                    primaryText = localizedString(R.string.settings_section_backup_and_restore_button_import_google_primary),
                    secondaryText = localizedString(R.string.settings_section_backup_and_restore_button_import_google_secondary),
                    onClick = { navController.navigate(ImportFromGoogleAuthScreenRoute) }
                )

                SectionButton(
                    painter = painterResource(R.drawable.aegis),
                    tint = colors.onBackground,
                    primaryText = localizedString(R.string.settings_section_backup_and_restore_button_import_aegis_primary),
                    secondaryText = localizedString(R.string.settings_section_backup_and_restore_button_import_aegis_secondary),
                    onClick = { navController.navigate(ImportFromAegisScreenRoute) }
                )
            }

            SectionGroup(
                modifier = Modifier.padding(all = 16.dp),
                title = localizedString(R.string.settings_section_about_and_support_title)
            ) {
                SectionButton(
                    painter = if (themeViewModel.theme == ThemeOption.SYSTEM_DEFAULT.value) {
                        if (themeViewModel.isSystemDark(context)) {
                            painterResource(R.drawable.logo_icon_dark_clipped)
                        } else {
                            painterResource(R.drawable.logo_icon_light_clipped)
                        }
                    } else {
                    when (themeViewModel.theme) {
                        ThemeOption.LIGHT.value -> painterResource(R.drawable.logo_icon_light_clipped)
                        ThemeOption.DARK.value -> painterResource(R.drawable.logo_icon_dark_clipped)
                        else -> null
                    }
                },
                    primaryText = "979",
                    secondaryText = localizedString(R.string.settings_section_about_and_support_button_979_secondary),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://979.st/en/about".toUri())
                        context.startActivity(intent)
                    }
                )

                SectionButton(
                    painter = painterResource(R.drawable.github),
                    tint = colors.onBackground,
                    primaryText = localizedString(R.string.settings_section_about_and_support_button_source_primary),
                    secondaryText = localizedString(R.string.settings_section_about_and_support_button_source_secondary),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://github.com/979st/2fa-android".toUri())
                        context.startActivity(intent)
                    }
                )

                SectionButton(
                    imageVector = Icons.Default.Description,
                    primaryText = localizedString(R.string.settings_section_about_and_support_button_log_primary),
                    onClick = {
                        Logger.i("SettingsScreen", "Downloading app log")
                        internalAppLogLauncher.launch("2fa_log")
                    }
                )
            }

            Spacer(modifier = Modifier.height(192.dp))
        }
    }

    SettingsAppBar()
}
