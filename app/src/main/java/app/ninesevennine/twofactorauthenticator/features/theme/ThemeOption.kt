package app.ninesevennine.twofactorauthenticator.features.theme

enum class ThemeOption(val value: Int) {
    SYSTEM_DEFAULT(0),
    LIGHT(1),
    DARK(2);
    companion object {
        private val valueMap = entries.associateBy { it.value }
        fun fromInt(value: Int) = valueMap[value] ?: SYSTEM_DEFAULT
    }
}