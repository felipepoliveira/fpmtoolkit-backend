package io.felipepoliveira.fpmtoolkit.security

import org.springframework.security.crypto.bcrypt.BCrypt

/**
 * Represent the safety rank of a password
 */
enum class PasswordRank(
    private val safetyRank: Int
) {
    NotAcceptable(-1),
    Acceptable(0),
    Safe(1),
    ;

    /**
     * Return a flag indicating if this rank is at least at the same level or greater than the
     * given rank
     */
    fun isAtLeast(aRank: PasswordRank) = this.safetyRank >= aRank.safetyRank
}

/**
 * Calculate the password rank of the given input
 */
fun calculatePasswordRank(password: String): PasswordRank {

    // At least it should have 8 characters
    if (password.length < 6) {
        return PasswordRank.NotAcceptable
    }

    // Safety char flags
    var hasLowerCaseCharacter = false
    var hasUpperCaseCharacter = false
    var hasDigit = false
    var hasNonBlankSymbol = false

    // Evaluate safety char flags
    for (c in password) {
        if (!hasLowerCaseCharacter && Character.isLowerCase(c)) {
            hasLowerCaseCharacter = true
            continue
        }
        else if (!hasUpperCaseCharacter && Character.isUpperCase(c)) {
            hasUpperCaseCharacter = true
            continue
        }
        else if (!hasDigit && Character.isDigit(c)) {
            hasDigit = true
            continue
        }
        else if (!hasNonBlankSymbol &&
            !Character.isSpaceChar(c) && !Character.isAlphabetic(c.code) && !Character.isDigit(c)) {
            hasNonBlankSymbol = true
            continue
        }
    }

    // Return the password rank
    return if (hasLowerCaseCharacter && hasUpperCaseCharacter && hasDigit && hasNonBlankSymbol) {
        PasswordRank.Acceptable
    } else {
        PasswordRank.NotAcceptable
    }
}

/**
 * Verify if the given password matches with the hashed password based on the secure algorithm used in the
 * platform
 */
fun comparePassword(password: String, hashedPassword: String): Boolean = BCrypt.checkpw(password, hashedPassword)

/**
 * Hash the given password into the default platform password hashing algorithm
 */
fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(7))