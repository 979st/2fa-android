package app.ninesevennine.twofactorauthenticator.features.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.OtpCardColors
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.OtpCardPalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpDarkBluePalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpDarkBrownPalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpDarkGreenPalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpDarkOrangePalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpDarkPinkPalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpDarkRedPalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpLightBluePalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpLightBrownPalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpLightGreenPalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpLightOrangePalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpLightPinkPalette
import app.ninesevennine.twofactorauthenticator.ui.elements.otpcard.otpLightRedPalette
import app.ninesevennine.twofactorauthenticator.utils.Logger

class ThemeViewModel(
    @Suppress("StaticFieldLeak")
    private val context: Context
) : ViewModel() {
    var themeOption by mutableStateOf(ThemeOption.SYSTEM_DEFAULT)
        private set

    var theme by mutableIntStateOf(ThemeOption.LIGHT.value)
        private set

    var colors by mutableStateOf(lightColorScheme())
        private set

    private val isSystemDark: Boolean
        get() = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    fun getOtpCardColors(otpCardColor: OtpCardColors): OtpCardPalette {
        val dark = when (themeOption) {
            ThemeOption.LIGHT -> false
            ThemeOption.DARK -> true
            else -> isSystemDark
        }

        return when (otpCardColor) {
            OtpCardColors.RED -> if (dark) otpDarkRedPalette() else otpLightRedPalette()
            OtpCardColors.GREEN -> if (dark) otpDarkGreenPalette() else otpLightGreenPalette()
            OtpCardColors.BLUE -> if (dark) otpDarkBluePalette() else otpLightBluePalette()
            OtpCardColors.PINK -> if (dark) otpDarkPinkPalette() else otpLightPinkPalette()
            OtpCardColors.ORANGE -> if (dark) otpDarkOrangePalette() else otpLightOrangePalette()
            OtpCardColors.BROWN -> if (dark) otpDarkBrownPalette() else otpLightBrownPalette()
        }
    }

    fun updateTheme(activityContext: Context, themeOption: ThemeOption) {
        Logger.i("ThemeViewModel", "Updating theme to $themeOption")
        ThemeModel.saveTheme(context, themeOption)

        this.themeOption = themeOption

        val (colorScheme, themeValue) = when (themeOption) {
            ThemeOption.SYSTEM_DEFAULT -> if (isSystemDark)
                darkColorScheme() to ThemeOption.DARK.value
            else
                lightColorScheme() to ThemeOption.LIGHT.value

            ThemeOption.LIGHT -> lightColorScheme() to ThemeOption.LIGHT.value
            ThemeOption.DARK -> darkColorScheme() to ThemeOption.DARK.value
            ThemeOption.DYNAMIC -> if (isSystemDark)
                dynamicDarkColorScheme(activityContext) to ThemeOption.DYNAMIC.value
            else
                dynamicLightColorScheme(activityContext) to ThemeOption.DYNAMIC.value
        }

        colors = colorScheme
        theme = themeValue
    }


    private fun lightColorScheme() = lightColorScheme(
        background = Color(0xffffffff),
        onBackground = Color(0xff060606),
        primaryContainer = Color(0xffeeeeee),
        onPrimaryContainer = Color(0xff060606)
    )

    private fun darkColorScheme() = darkColorScheme(
        background = Color(0xff0b0b0b),
        onBackground = Color(0xffdedede),
        primaryContainer = Color(0xff161616),
        onPrimaryContainer = Color(0xffdedede)
    )
}