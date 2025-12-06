package app.ninesevennine.twofactorauthenticator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.features.locale.LocaleOption
import app.ninesevennine.twofactorauthenticator.localeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedRadioButton
import kotlinx.serialization.Serializable

@Serializable
object LanguageSelectionScreenRoute

@Composable
fun LanguageSelectionScreen() {
    val context = LocalContext.current
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

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                RoundedRadioButton(
                    painter = painterResource(R.drawable.flag_european_union),
                    label = languageLabelFromLocale(LocaleOption.EN_US.value),
                    enabled = localeViewModel.effectiveLocale == LocaleOption.EN_US.value,
                    onClick = { localeViewModel.updateLocale(context, LocaleOption.EN_US) }
                )
                RoundedRadioButton(
                    painter = painterResource(R.drawable.flag_spain),
                    label = languageLabelFromLocale(LocaleOption.ES_ES.value),
                    enabled = localeViewModel.effectiveLocale == LocaleOption.ES_ES.value,
                    onClick = { localeViewModel.updateLocale(context, LocaleOption.ES_ES) }
                )
                RoundedRadioButton(
                    painter = painterResource(R.drawable.flag_russia),
                    label = languageLabelFromLocale(LocaleOption.RU_RU.value),
                    enabled = localeViewModel.effectiveLocale == LocaleOption.RU_RU.value,
                    onClick = { localeViewModel.updateLocale(context, LocaleOption.RU_RU) }
                )
                RoundedRadioButton(
                    painter = painterResource(R.drawable.flag_germany),
                    label = languageLabelFromLocale(LocaleOption.DE_DE.value),
                    enabled = localeViewModel.effectiveLocale == LocaleOption.DE_DE.value,
                    onClick = { localeViewModel.updateLocale(context, LocaleOption.DE_DE) }
                )
                RoundedRadioButton(
                    painter = painterResource(R.drawable.flag_france),
                    label = languageLabelFromLocale(LocaleOption.FR_FR.value),
                    enabled = localeViewModel.effectiveLocale == LocaleOption.FR_FR.value,
                    onClick = { localeViewModel.updateLocale(context, LocaleOption.FR_FR) }
                )
                RoundedRadioButton(
                    painter = painterResource(R.drawable.flag_vietnam),
                    label = languageLabelFromLocale(LocaleOption.VI_VN.value),
                    enabled = localeViewModel.effectiveLocale == LocaleOption.VI_VN.value,
                    onClick = { localeViewModel.updateLocale(context, LocaleOption.VI_VN) }
                )
            }
        }
    }
}

fun languageLabelFromLocale(locale: String): String {
    return when (locale) {
        LocaleOption.EN_US.value -> "English International"
        LocaleOption.ES_ES.value -> "Español"
        LocaleOption.RU_RU.value -> "Русский"
        LocaleOption.DE_DE.value -> "Deutsch"
        LocaleOption.FR_FR.value -> "Français"
        LocaleOption.VI_VN.value -> "Tiếng Việt"
        else -> "Unknown"
    }
}