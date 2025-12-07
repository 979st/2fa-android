package app.ninesevennine.twofactorauthenticator.ui

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenLockPortrait
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
import app.ninesevennine.twofactorauthenticator.features.locale.LocaleOption
import app.ninesevennine.twofactorauthenticator.features.locale.localizedString
import app.ninesevennine.twofactorauthenticator.features.theme.ThemeOption
import app.ninesevennine.twofactorauthenticator.localeViewModel
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.SectionButton
import app.ninesevennine.twofactorauthenticator.ui.elements.SectionGroup
import app.ninesevennine.twofactorauthenticator.ui.elements.WideTitle
import app.ninesevennine.twofactorauthenticator.ui.elements.bottomappbar.SettingsAppBar
import app.ninesevennine.twofactorauthenticator.ui.elements.widebutton.WideButtonWithIcon
import app.ninesevennine.twofactorauthenticator.ui.elements.widebutton.WideButtonWithTintedIcon
import app.ninesevennine.twofactorauthenticator.ui.elements.wideradiobutton.WideRadioButton
import app.ninesevennine.twofactorauthenticator.ui.elements.wideradiobutton.WideRadioButtonWithIcon
import app.ninesevennine.twofactorauthenticator.ui.elements.wideradiobutton.WideRadioButtonWithTintedIcon
import app.ninesevennine.twofactorauthenticator.utils.Logger
import kotlinx.serialization.Serializable

@Serializable
object SettingsScreenRoute

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val localeViewModel = context.localeViewModel

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

//            SectionGroup("Appearance") {
//                SectionButton(
//                    imageVector = Icons.Default.Language,
//                    primaryText = "Language",
//                    secondaryText = languageLabelFromLocale(localeViewModel.effectiveLocale),
//                    onClick = { navController.navigate(LanguageSelectionScreenRoute) }
//                )
//                SectionButton(primaryText = "Theme", secondaryText = "Light")
//                SectionButton(primaryText = "Card style", secondaryText = "Classic")
//
//                val requireTapToReveal = context.configViewModel.values.requireTapToReveal
//                SectionButton(
//                    imageVector = Icons.Default.TouchApp,
//                    primaryText = "Tap to reveal codes",
//                    enabled = requireTapToReveal,
//                    onClick = { context.configViewModel.updateTapToReveal(!requireTapToReveal) }
//                )
//            }
//
//            SectionGroup("Exploit protection") {
//                SectionButton(primaryText = "Pixnapping")
//            }

            LanguageSettingsSection()

            ThemeSettingsSection()

            CardTypeSettingsSection()

            BackupSettingsSection()

            OtherSettingsScreen()

            AboutSettingsSection()

            Spacer(modifier = Modifier.height(192.dp))
        }
    }

    SettingsAppBar()
}

@Composable
private fun LanguageSettingsSection() {
    val context = LocalContext.current
    val localeViewModel = context.localeViewModel

    WideTitle(text = localizedString(R.string.settings_option_language))

    WideRadioButtonWithTintedIcon(
        icon = painterResource(id = R.drawable.flag_european_union),
        tint = Color.Unspecified,
        label = "English International",
        enabled = localeViewModel.effectiveLocale == LocaleOption.EN_US.value,
        onClick = { localeViewModel.updateLocale(context, LocaleOption.EN_US) }
    )

    WideRadioButtonWithTintedIcon(
        icon = painterResource(id = R.drawable.flag_spain),
        tint = Color.Unspecified,
        label = "Español",
        enabled = localeViewModel.effectiveLocale == LocaleOption.ES_ES.value,
        onClick = { localeViewModel.updateLocale(context, LocaleOption.ES_ES) }
    )

    WideRadioButtonWithTintedIcon(
        icon = painterResource(id = R.drawable.flag_russia),
        tint = Color.Unspecified,
        label = "Русский",
        enabled = localeViewModel.effectiveLocale == LocaleOption.RU_RU.value,
        onClick = { localeViewModel.updateLocale(context, LocaleOption.RU_RU) }
    )

    WideRadioButtonWithTintedIcon(
        icon = painterResource(id = R.drawable.flag_germany),
        tint = Color.Unspecified,
        label = "Deutsch",
        enabled = localeViewModel.effectiveLocale == LocaleOption.DE_DE.value,
        onClick = { localeViewModel.updateLocale(context, LocaleOption.DE_DE) }
    )

    WideRadioButtonWithTintedIcon(
        icon = painterResource(id = R.drawable.flag_france),
        tint = Color.Unspecified,
        label = "Français",
        enabled = localeViewModel.effectiveLocale == LocaleOption.FR_FR.value,
        onClick = { localeViewModel.updateLocale(context, LocaleOption.FR_FR) }
    )

    WideRadioButtonWithTintedIcon(
        icon = painterResource(id = R.drawable.flag_vietnam),
        tint = Color.Unspecified,
        label = "Tiếng Việt",
        enabled = localeViewModel.effectiveLocale == LocaleOption.VI_VN.value,
        onClick = { localeViewModel.updateLocale(context, LocaleOption.VI_VN) }
    )

    WideButtonWithIcon(
        icon = Icons.Default.Refresh,
        label = localizedString(R.string.common_use_system_default),
        onClick = { localeViewModel.updateLocale(context, LocaleOption.SYSTEM_DEFAULT) }
    )
}

@Composable
private fun ThemeSettingsSection() {
    val context = LocalContext.current
    val themeViewModel = context.themeViewModel

    Spacer(modifier = Modifier.height(16.dp))
    WideTitle(text = localizedString(R.string.settings_option_appearance))

    WideRadioButtonWithIcon(
        icon = Icons.Filled.LightMode,
        label = localizedString(R.string.settings_appearance_light),
        enabled = themeViewModel.theme == ThemeOption.LIGHT.value,
        onClick = { themeViewModel.updateTheme(context, ThemeOption.LIGHT) }
    )

    WideRadioButtonWithIcon(
        icon = Icons.Filled.DarkMode,
        label = localizedString(R.string.settings_appearance_dark),
        enabled = themeViewModel.theme == ThemeOption.DARK.value,
        onClick = { themeViewModel.updateTheme(context, ThemeOption.DARK) }
    )

    WideRadioButtonWithIcon(
        icon = Icons.Filled.Contrast,
        label = localizedString(R.string.settings_appearance_dynamic),
        enabled = themeViewModel.theme == ThemeOption.DYNAMIC.value,
        onClick = { themeViewModel.updateTheme(context, ThemeOption.DYNAMIC) }
    )

    WideButtonWithIcon(
        icon = Icons.Default.Refresh,
        label = localizedString(R.string.common_use_system_default),
        onClick = { themeViewModel.updateTheme(context, ThemeOption.SYSTEM_DEFAULT) }
    )
}

@Composable
private fun CardTypeSettingsSection() {
    val context = LocalContext.current
    val configViewModel = context.configViewModel

    Spacer(modifier = Modifier.height(16.dp))
    WideTitle(text = localizedString(R.string.settings_option_card_style))

    WideRadioButton(
        label = localizedString(R.string.settings_card_style_classic),
        enabled = configViewModel.values.cardStyle == 0,
        onClick = { configViewModel.updateCardStyle(0) }
    )

    WideRadioButton(
        label = localizedString(R.string.settings_card_style_minimal),
        enabled = configViewModel.values.cardStyle == 1,
        onClick = { configViewModel.updateCardStyle(1) }
    )
}

@Composable
private fun BackupSettingsSection() {
    val navController = LocalNavController.current

    Spacer(modifier = Modifier.height(16.dp))
    WideTitle(text = localizedString(R.string.settings_section_manage_data))

    WideButtonWithIcon(
        icon = Icons.Filled.Upload,
        label = localizedString(R.string.settings_button_backup_codes),
        onClick = { navController.navigate(BackupVaultScreenRoute) }
    )

    WideButtonWithIcon(
        icon = Icons.Filled.Download,
        label = localizedString(R.string.settings_button_restore_codes),
        onClick = { navController.navigate(RestoreVaultScreenRoute) }
    )

    WideButtonWithTintedIcon(
        icon = painterResource(R.drawable.icon_google_authenticator),
        tint = Color.Unspecified,
        label = localizedString(R.string.settings_button_text_import_google_authenticator),
        onClick = { navController.navigate(ImportFromGoogleAuthScreenRoute) }
    )

    WideButtonWithTintedIcon(
        icon = painterResource(R.drawable.icon_google_authenticator),
        tint = Color.Unspecified,
        label = localizedString(R.string.settings_button_export_google_authenticator),
        onClick = { navController.navigate(ExportToGoogleAuthScreenRoute) }
    )

    WideButtonWithIcon(
        icon = painterResource(R.drawable.aegis),
        label = localizedString(R.string.settings_button_text_import_aegis),
        onClick = { navController.navigate(ImportFromAegisScreenRoute) }
    )
}

@Composable
private fun OtherSettingsScreen() {
    val context = LocalContext.current

    Spacer(modifier = Modifier.height(16.dp))
    WideTitle(text = localizedString(R.string.settings_section_other))

    val antiPixnapping = context.configViewModel.values.antiPixnapping
    WideRadioButtonWithTintedIcon(
        icon = painterResource(R.drawable.bug),
        tint = Color.Unspecified,
        label = "Anti-Pixnapping (CVE-2025-48561)",
        enabled = antiPixnapping,
        onClick = { context.configViewModel.updateAntiPixnapping(!antiPixnapping) }
    )

    val screenSecurity = context.configViewModel.values.screenSecurity
    WideRadioButtonWithIcon(
        icon = Icons.Default.ScreenLockPortrait,
        label = localizedString(R.string.settings_option_screen_security),
        enabled = screenSecurity,
        onClick = { context.configViewModel.updateScreenSecurity(!screenSecurity) }
    )

    val requireTapToReveal = context.configViewModel.values.requireTapToReveal
    WideRadioButtonWithIcon(
        icon = Icons.Default.TouchApp,
        label = localizedString(R.string.settings_option_tap_to_reveal),
        enabled = requireTapToReveal,
        onClick = { context.configViewModel.updateTapToReveal(!requireTapToReveal) }
    )

    val enableFocusSearch = context.configViewModel.values.enableFocusSearch
    WideRadioButtonWithIcon(
        icon = painterResource(R.drawable.frame_inspect),
        label = localizedString(R.string.settings_option_focus_search),
        enabled = enableFocusSearch,
        onClick = { context.configViewModel.updateFocusSearch(!enableFocusSearch) }
    )
}

@Composable
private fun AboutSettingsSection() {
    val context = LocalContext.current
    val navController = LocalNavController.current

    Spacer(modifier = Modifier.height(16.dp))
    WideTitle(text = localizedString(R.string.settings_section_about))

    WideButtonWithTintedIcon(
        icon = painterResource(R.drawable.issuer_discord),
        tint = Color.Unspecified,
        label = "Discord",
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, "https://discord.com/invite/zxgXNzhYJu".toUri())
            context.startActivity(intent)
        }
    )

    WideButtonWithTintedIcon(
        icon = painterResource(R.drawable.bluesky),
        tint = Color.Unspecified,
        label = "Bluesky",
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, "https://bsky.app/profile/979.st".toUri())
            context.startActivity(intent)
        }
    )

    WideButtonWithIcon(
        icon = painterResource(R.drawable.github),
        label = localizedString(R.string.about_button_view_source),
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, "https://github.com/979st/2fa-android".toUri())
            context.startActivity(intent)
        }
    )

    WideButtonWithIcon(
        icon = Icons.Filled.Code,
        label = localizedString(R.string.about_button_debug_log),
        onClick = {
            Logger.i("SettingsScreen", "Opened debug log")
            navController.navigate(LogScreenRoute)
        }
    )
}
