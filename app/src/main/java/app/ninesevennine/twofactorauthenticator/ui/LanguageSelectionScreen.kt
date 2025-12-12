package app.ninesevennine.twofactorauthenticator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.configViewModel
import app.ninesevennine.twofactorauthenticator.features.locale.LocaleOption
import app.ninesevennine.twofactorauthenticator.localeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedButton
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedRadioButton
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedRefreshButton
import kotlinx.serialization.Serializable

@Serializable
object LanguageSelectionScreenRoute

@Composable
fun LanguageSelectionScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val configViewModel = context.configViewModel
    val localeViewModel = context.localeViewModel

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    var showRefreshButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showRefreshButton = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxHeight()
                .padding(horizontal = navPadding)
                .padding(bottom = navPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

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

                AnimatedVisibility(
                    visible = showRefreshButton && configViewModel.values.locale != LocaleOption.SYSTEM_DEFAULT,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> -fullHeight / 4 },
                        animationSpec = spring(stiffness = 250f, dampingRatio = 0.85f)
                    ) + scaleIn(
                        initialScale = 0.9f,
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = 300)
                    ) + scaleOut(
                        targetScale = 0.9f,
                        animationSpec = tween(durationMillis = 300)
                    )
                ) {
                    RoundedRefreshButton(
                        imageVector = Icons.Default.Refresh,
                        label = "Use System Default",
                        onClick = { localeViewModel.updateLocale(context, LocaleOption.SYSTEM_DEFAULT) }
                    )
                }
            }

            RoundedButton(
                label = "Done",
                onClick = { navController.popBackStack()}
            )
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