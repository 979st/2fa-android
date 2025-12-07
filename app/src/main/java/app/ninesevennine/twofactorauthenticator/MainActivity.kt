package app.ninesevennine.twofactorauthenticator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import app.ninesevennine.twofactorauthenticator.features.qrscanner.ZXingQrUri
import app.ninesevennine.twofactorauthenticator.features.theme.ThemeOption
import app.ninesevennine.twofactorauthenticator.ui.elements.UseIncognitoKeyboard
import kotlinx.coroutines.launch

val LocalNavController =
    staticCompositionLocalOf<NavHostController> { error("NavController not provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntents(intent)

        enableEdgeToEdge()
        setContent {
            val navController: NavHostController = rememberNavController()

            LaunchedEffect(themeViewModel.theme) {
                val isDarkTheme = themeViewModel.theme == ThemeOption.DARK.value
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.isAppearanceLightStatusBars = !isDarkTheme
                controller.isAppearanceLightNavigationBars = !isDarkTheme
            }

            if (configViewModel.values.screenSecurity) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            localeViewModel.updateLocale(this, configViewModel.values.locale)
            themeViewModel.updateTheme(this, configViewModel.values.theme)

            CompositionLocalProvider(
                LocalNavController provides navController
            ) {
                UseIncognitoKeyboard {
                    AppNavigation()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vaultViewModel.load(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntents(intent)
    }

    private fun handleIntents(intent: Intent?) {
        if (intent == null)
            return

        val action = intent.action
        val data = intent.data

        when (action) {
            Intent.ACTION_VIEW -> {
                data?.let {
                    if (it.scheme == "otpauth") {
                        configViewModel.otpauthUrl = data.toString()
                    }
                }
            }

            Intent.ACTION_SEND -> {
                val uri: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                if (uri != null && (intent.type?.startsWith("image/") ?: false)) {
                    lifecycleScope.launch {
                        val url = ZXingQrUri.decode(uri, this@MainActivity.contentResolver)
                        url?.let { decodedUrl ->
                            configViewModel.otpauthUrl = decodedUrl
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        vaultViewModel.save(this)
        configViewModel.save(this)
    }
}
