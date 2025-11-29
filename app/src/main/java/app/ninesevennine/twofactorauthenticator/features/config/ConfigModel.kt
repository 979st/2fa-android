package app.ninesevennine.twofactorauthenticator.features.config

import android.content.Context
import android.util.Log
import app.ninesevennine.twofactorauthenticator.features.locale.LocaleOption
import app.ninesevennine.twofactorauthenticator.features.theme.ThemeOption
import org.json.JSONObject
import java.io.File

object ConfigModel {
    private const val FILE_NAME = "config.json"

    data class Config(
        var locale: LocaleOption = LocaleOption.SYSTEM_DEFAULT,
        var theme: ThemeOption = ThemeOption.SYSTEM_DEFAULT,
        var requireTapToReveal: Boolean = false,
        var enableFocusSearch: Boolean = false,
        var screenSecurity: Boolean = true,
        var antiPixnapping: Boolean = false,
        var cardStyle: Int = 0
    ) {
        fun save(context: Context) {
            runCatching {
                JSONObject().apply {
                    put("version", 1)
                    put("locale", locale.value)
                    put("theme", theme.value)
                    put("requireTapToReveal", requireTapToReveal)
                    put("enableFocusSearch", enableFocusSearch)
                    put("screenSecurity", screenSecurity)
                    put("antiPixnapping", antiPixnapping)
                    put("cardStyle", cardStyle)
                }.toString().let { jsonString ->
                    File(context.noBackupFilesDir, FILE_NAME)
                        .writeText(jsonString, Charsets.UTF_8)
                }
            }.onFailure { e ->
                Log.e("ConfigModel", "Failed saving config", e)
            }
        }

        companion object {
            fun load(context: Context): Config =
                runCatching {
                    val file = File(context.noBackupFilesDir, FILE_NAME)

                    if (!file.exists() || file.length() == 0L) return Config()

                    val jsonString = file.readText(Charsets.UTF_8)
                    if (jsonString.isBlank()) return Config()

                    val json = JSONObject(jsonString)

                    Config(
                        locale = LocaleOption.fromString(json.getString("locale")),
                        theme = ThemeOption.fromInt(json.getInt("theme")),
                        requireTapToReveal = json.getBoolean("requireTapToReveal"),
                        enableFocusSearch = json.getBoolean("enableFocusSearch"),
                        screenSecurity = json.getBoolean("screenSecurity"),
                        antiPixnapping = json.getBoolean("antiPixnapping"),
                        cardStyle = json.optInt("cardStyle", 0)
                    )
                }.getOrElse { e ->
                    Log.e("ConfigModel", "Failed loading config", e)
                    Config()
                }
        }
    }
}