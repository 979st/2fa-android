package app.ninesevennine.twofactorauthenticator.ui

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.R
import app.ninesevennine.twofactorauthenticator.configViewModel
import app.ninesevennine.twofactorauthenticator.features.locale.localizedString
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedButton
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedRadioButton
import kotlinx.serialization.Serializable

@Serializable
object CardStyleSelectionScreenRoute

@Composable
fun CardStyleSelectionScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val configViewModel = context.configViewModel

    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

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
                    imageVector = Icons.Default.Star,
                    label = localizedString(R.string.card_style_option_classic),
                    enabled = configViewModel.values.cardStyle == 0,
                    onClick = { configViewModel.updateCardStyle(0) }
                )

                RoundedRadioButton(
                    imageVector = Icons.Default.Star,
                    label = localizedString(R.string.card_style_option_minimalist),
                    enabled = configViewModel.values.cardStyle == 1,
                    onClick = { configViewModel.updateCardStyle(1) }
                )
            }

            RoundedButton(
                label = localizedString(R.string.card_style_button_go_back),
                onClick = { navController.popBackStack()}
            )
        }
    }
}