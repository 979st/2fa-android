package app.ninesevennine.twofactorauthenticator.utils

object Password {
    fun isLong(password: String): Boolean {
        return password.length >= 8
    }

    fun hasUppercase(password: String): Boolean {
        return password.any { it.isUpperCase() }
    }

    fun hasDigit(password: String): Boolean {
        return password.any { it.isDigit() }
    }

    fun hasSpecial(password: String): Boolean {
        return password.any { !it.isLetterOrDigit() }
    }

    fun hasNoLeadingOrTrailingWhitespace(password: String): Boolean {
        return password == password.trim()
    }

    fun isValid(password: String): Boolean {
        return isLong(password)
                && hasUppercase(password)
                && hasDigit(password)
                && hasSpecial(password)
                && hasNoLeadingOrTrailingWhitespace(password)
    }
}