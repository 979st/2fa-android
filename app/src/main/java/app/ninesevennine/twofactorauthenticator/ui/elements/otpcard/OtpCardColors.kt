package app.ninesevennine.twofactorauthenticator.ui.elements.otpcard

import kotlin.random.Random

enum class OtpCardColors(val value: String) {
    RED("RED"),
    ORANGE("ORANGE"),
    PINK("PINK"),
    BLUE("BLUE"),
    GREEN("GREEN"),
    BROWN("BROWN");

    companion object {
        private val entriesMap = entries.associateBy { it.value }

        fun fromString(value: String) = entriesMap[value] ?: random()
        fun random(): OtpCardColors {
            val values = entries
            return values[Random.nextInt(values.size)]
        }
    }
}