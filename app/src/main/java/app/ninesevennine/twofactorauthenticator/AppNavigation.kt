package app.ninesevennine.twofactorauthenticator

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import app.ninesevennine.twofactorauthenticator.ui.BackupVaultScreen
import app.ninesevennine.twofactorauthenticator.ui.BackupVaultScreenRoute
import app.ninesevennine.twofactorauthenticator.ui.EditScreen
import app.ninesevennine.twofactorauthenticator.ui.EditScreenRoute
import app.ninesevennine.twofactorauthenticator.ui.ExportToGoogleAuthScreen
import app.ninesevennine.twofactorauthenticator.ui.ExportToGoogleAuthScreenRoute
import app.ninesevennine.twofactorauthenticator.ui.LogScreen
import app.ninesevennine.twofactorauthenticator.ui.LogScreenRoute
import app.ninesevennine.twofactorauthenticator.ui.MainScreen
import app.ninesevennine.twofactorauthenticator.ui.MainScreenRoute
import app.ninesevennine.twofactorauthenticator.ui.RestoreVaultScreen
import app.ninesevennine.twofactorauthenticator.ui.RestoreVaultScreenRoute
import app.ninesevennine.twofactorauthenticator.ui.SettingsScreen
import app.ninesevennine.twofactorauthenticator.ui.SettingsScreenRoute


@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val colors = context.themeViewModel.colors

    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        navController = navController,
        startDestination = MainScreenRoute,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        }
    ) {
        composable<MainScreenRoute> { MainScreen() }
        composable<SettingsScreenRoute> { SettingsScreen() }
        composable<EditScreenRoute> {
            val args = it.toRoute<EditScreenRoute>()
            EditScreen(args.uuidString)
        }
        composable<BackupVaultScreenRoute> { BackupVaultScreen() }
        composable<RestoreVaultScreenRoute> { RestoreVaultScreen() }
        composable<ExportToGoogleAuthScreenRoute> { ExportToGoogleAuthScreen() }
        composable<LogScreenRoute> { LogScreen() }
    }
}