package app.ninesevennine.twofactorauthenticator

import android.app.Application
import app.ninesevennine.twofactorauthenticator.features.config.ConfigViewModel
import app.ninesevennine.twofactorauthenticator.features.crypto.SecureCrypto
import app.ninesevennine.twofactorauthenticator.features.locale.LocaleViewModel
import app.ninesevennine.twofactorauthenticator.features.theme.ThemeViewModel
import app.ninesevennine.twofactorauthenticator.features.vault.VaultViewModel
import app.ninesevennine.twofactorauthenticator.utils.Logger
import app.ninesevennine.twofactorauthenticator.utils.System

class MyApplication : Application() {
    val configViewModel = ConfigViewModel()
    val localeViewModel = LocaleViewModel()
    val themeViewModel = ThemeViewModel()
    val vaultViewModel = VaultViewModel()


    override fun onCreate() {
        super.onCreate()

        Logger.initialize(this)

        Logger.i("APP", "APPLICATION_ID = ${BuildConfig.APPLICATION_ID}")
        Logger.i("APP", "BUILD_TYPE = ${BuildConfig.BUILD_TYPE}")
        Logger.i("APP", "VERSION_CODE = ${BuildConfig.VERSION_CODE}")
        Logger.i("APP", "VERSION_NAME = ${BuildConfig.VERSION_NAME}")
        Logger.i(
            "SYSTEM",
            "ro.build.fingerprint = ${System.getSystemProperty("ro.build.fingerprint")}"
        )

        configViewModel.load(this)

        SecureCrypto.initialize(this)

        localeViewModel.updateLocale(this, configViewModel.values.locale)
        themeViewModel.updateTheme(this, configViewModel.values.theme)

        vaultViewModel.load(this)
    }
}