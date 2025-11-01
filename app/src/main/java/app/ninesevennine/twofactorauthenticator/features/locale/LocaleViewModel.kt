package app.ninesevennine.twofactorauthenticator.features.locale

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.ninesevennine.twofactorauthenticator.configViewModel
import app.ninesevennine.twofactorauthenticator.utils.Logger
import java.util.Locale

class LocaleViewModel() : ViewModel() {
    private var _effectiveLocale by mutableStateOf(LocaleOption.EN_US.value)
    val effectiveLocale: String get() = _effectiveLocale

    private val stringCache = mutableMapOf<Int, String>()

    private data class PluralCacheKey(
        val resourceId: Int, val quantity: Int, val formatArgs: List<Any>
    )

    private val pluralCache = mutableMapOf<PluralCacheKey, String>()

    fun getLocalizedString(context: Context, @StringRes resourceId: Int): String {
        return stringCache.getOrPut(resourceId) {
            Configuration(context.resources.configuration).run {
                setLocale(Locale.forLanguageTag(effectiveLocale))
                context.createConfigurationContext(this).getString(resourceId)
            }
        }
    }

    fun getLocalizedString(
        context: Context, @StringRes resourceId: Int, vararg formatArgs: Any
    ): String {
        Configuration(context.resources.configuration).run {
            setLocale(Locale.forLanguageTag(effectiveLocale))
            return context.createConfigurationContext(this).getString(resourceId, *formatArgs)
        }
    }

    fun getQuantityString(context: Context, @PluralsRes resourceId: Int, quantity: Int): String {
        val key = PluralCacheKey(resourceId, quantity, emptyList())
        return pluralCache.getOrPut(key) {
            Configuration(context.resources.configuration).run {
                setLocale(Locale.forLanguageTag(effectiveLocale))
                context.createConfigurationContext(this).resources.getQuantityString(
                    resourceId, quantity
                )
            }
        }
    }

    fun getQuantityString(
        context: Context, @PluralsRes resourceId: Int, quantity: Int, vararg formatArgs: Any
    ): String {
        Configuration(context.resources.configuration).run {
            setLocale(Locale.forLanguageTag(effectiveLocale))
            return context.createConfigurationContext(this).resources.getQuantityString(
                resourceId, quantity, *formatArgs
            )
        }
    }

    fun updateLocale(context: Context, newOption: LocaleOption) {
        Logger.i("LocaleViewModel", "updateLocale $newOption")

        val newEffectiveLocale = computeEffectiveLocale(newOption)
        if (_effectiveLocale != newEffectiveLocale) {
            _effectiveLocale = newEffectiveLocale
            stringCache.clear()
            pluralCache.clear()
        }

        context.configViewModel.updateLocale(newOption)
    }

    private fun computeEffectiveLocale(option: LocaleOption): String = when (option) {
        LocaleOption.SYSTEM_DEFAULT -> {
            LocaleOption.fromLanguageOrDefault(
                Resources.getSystem().configuration.locales[0]?.language ?: LocaleOption.EN_US.value
            ).value
        }

        else -> option.value
    }
}