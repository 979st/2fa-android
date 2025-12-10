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
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.ninesevennine.twofactorauthenticator.LocalNavController
import app.ninesevennine.twofactorauthenticator.configViewModel
import app.ninesevennine.twofactorauthenticator.features.theme.ThemeOption
import app.ninesevennine.twofactorauthenticator.themeViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedButton
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedRadioButton
import app.ninesevennine.twofactorauthenticator.ui.elements.RoundedRefreshButton
import kotlinx.serialization.Serializable

@Serializable
object ThemeSelectionScreenRoute

@Composable
fun ThemeSelectionScreen() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val themeViewModel = context.themeViewModel
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
                    imageVector = Icons.Default.LightMode,
                    label = "Light",
                    enabled = themeViewModel.theme == ThemeOption.LIGHT.value,
                    onClick = { themeViewModel.updateTheme(context, ThemeOption.LIGHT) }
                )

                RoundedRadioButton(
                    imageVector = Icons.Default.DarkMode,
                    label = "Dark",
                    enabled = themeViewModel.theme == ThemeOption.DARK.value,
                    onClick = { themeViewModel.updateTheme(context, ThemeOption.DARK) }
                )

                RoundedRadioButton(
                    imageVector = Icons.Default.Contrast,
                    label = "Dynamic",
                    enabled = themeViewModel.theme == ThemeOption.DYNAMIC.value,
                    onClick = { themeViewModel.updateTheme(context, ThemeOption.DYNAMIC) }
                )

                AnimatedVisibility(
                    visible = configViewModel.values.theme != ThemeOption.SYSTEM_DEFAULT,
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
                        onClick = { themeViewModel.updateTheme(context, ThemeOption.SYSTEM_DEFAULT) }
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