package app.ninesevennine.twofactorauthenticator.features.config

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.ninesevennine.twofactorauthenticator.features.locale.LocaleOption
import app.ninesevennine.twofactorauthenticator.features.theme.ThemeOption

class ConfigViewModel : ViewModel() {
    var values by mutableStateOf(ConfigModel.Config())
        private set

    var otpauthUrl by mutableStateOf("")

    fun save(context: Context) = values.save(context)
    fun load(context: Context) = ConfigModel.Config.load(context).also { values = it }

    fun updateLocale(value: LocaleOption) {
        values = values.copy(locale = value)
    }

    fun updateTheme(value: ThemeOption) {
        values = values.copy(theme = value)
    }

    fun updateTapToReveal(value: Boolean) {
        values = values.copy(requireTapToReveal = value)
    }

    fun updateFocusSearch(value: Boolean) {
        values = values.copy(enableFocusSearch = value)
    }

    fun updateScreenSecurity(value: Boolean) {
        values = values.copy(screenSecurity = value)
    }

    fun updateAntiPixnapping(value: Boolean) {
        values = values.copy(antiPixnapping = value)
    }
}