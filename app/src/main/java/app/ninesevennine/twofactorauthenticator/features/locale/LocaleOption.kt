package app.ninesevennine.twofactorauthenticator.features.locale

enum class LocaleOption(val value: String) {
    SYSTEM_DEFAULT("default"),
    EN_US("en-US"),
    ES_ES("es-ES"),
    RU_RU("ru-RU");

    companion object {
        private val entriesMap = entries.associateBy { it.value }
        private val languageMap = entries
            .filter { it != SYSTEM_DEFAULT }
            .groupBy { it.value.substringBefore("-") }

        fun fromString(value: String) = entriesMap[value] ?: SYSTEM_DEFAULT

        fun fromLanguageOrDefault(languageCode: String) =
            languageMap[languageCode.lowercase()]?.firstOrNull() ?: EN_US
    }
}