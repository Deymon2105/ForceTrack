package com.example.forcetrack.utils

/**
 * Utilidades para validación de formularios
 */
object ValidationUtils {

    // Constantes de validación
    private const val MIN_PASSWORD_LENGTH = 6
    private const val MIN_USERNAME_LENGTH = 3
    private const val MAX_USERNAME_LENGTH = 20

    /**
     * Valida el formato de un correo electrónico
     */
    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    /**
     * Valida la longitud y formato de la contraseña
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= MIN_PASSWORD_LENGTH
    }

    /**
     * Obtiene mensaje de error para contraseña inválida
     */
    fun getPasswordErrorMessage(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña no puede estar vacía"
            password.length < MIN_PASSWORD_LENGTH -> "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
            else -> null
        }
    }

    /**
     * Valida el nombre de usuario
     */
    fun isValidUsername(username: String): Boolean {
        return username.length in MIN_USERNAME_LENGTH..MAX_USERNAME_LENGTH &&
               username.matches("[a-zA-Z0-9_]+".toRegex())
    }

    /**
     * Obtiene mensaje de error para nombre de usuario inválido
     */
    fun getUsernameErrorMessage(username: String): String? {
        return when {
            username.isBlank() -> "El nombre de usuario no puede estar vacío"
            username.length < MIN_USERNAME_LENGTH -> "El nombre de usuario debe tener al menos $MIN_USERNAME_LENGTH caracteres"
            username.length > MAX_USERNAME_LENGTH -> "El nombre de usuario no puede tener más de $MAX_USERNAME_LENGTH caracteres"
            !username.matches("[a-zA-Z0-9_]+".toRegex()) -> "El nombre de usuario solo puede contener letras, números y guiones bajos"
            else -> null
        }
    }

    /**
     * Obtiene mensaje de error para correo inválido
     */
    fun getEmailErrorMessage(email: String): String? {
        return when {
            email.isBlank() -> "El correo electrónico no puede estar vacío"
            !isValidEmail(email) -> "El formato del correo electrónico no es válido"
            else -> null
        }
    }

    /**
     * Valida todos los campos de registro
     */
    data class RegistrationValidation(
        val isValid: Boolean,
        val usernameError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null
    )

    fun validateRegistration(username: String, email: String, password: String): RegistrationValidation {
        val usernameError = getUsernameErrorMessage(username)
        val emailError = getEmailErrorMessage(email)
        val passwordError = getPasswordErrorMessage(password)

        return RegistrationValidation(
            isValid = usernameError == null && emailError == null && passwordError == null,
            usernameError = usernameError,
            emailError = emailError,
            passwordError = passwordError
        )
    }

    /**
     * Valida campos de login
     */
    data class LoginValidation(
        val isValid: Boolean,
        val emailError: String? = null,
        val passwordError: String? = null
    )

    fun validateLogin(email: String, password: String): LoginValidation {
        val emailError = getEmailErrorMessage(email)
        val passwordError = if (password.isBlank()) "La contraseña no puede estar vacía" else null

        return LoginValidation(
            isValid = emailError == null && passwordError == null,
            emailError = emailError,
            passwordError = passwordError
        )
    }
}
