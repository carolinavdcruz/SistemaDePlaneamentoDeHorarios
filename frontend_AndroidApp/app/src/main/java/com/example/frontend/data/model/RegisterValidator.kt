package com.example.frontend.data.model

object RegisterValidator {

    private const val PASSWORD_MINIMUM_LENGTH = 8
    private const val NAME_MINIMUM_LENGTH = 2

    private val passwordRegex =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@\$!%*?&]{$PASSWORD_MINIMUM_LENGTH,}$".toRegex()

    private val emailRegex =
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun validate(name: String, email: String, password: String): String? {
        val normalizedName  = name.trim()
        val normalizedEmail = email.trim()

        return when {
            normalizedName.isBlank() -> "O nome e obrigatorio."
            normalizedName.length < NAME_MINIMUM_LENGTH ->
                "O nome deve ter pelo menos $NAME_MINIMUM_LENGTH caracteres."
            normalizedEmail.isBlank() -> "O email e obrigatorio."
            !normalizedEmail.matches(emailRegex) -> "O email introduzido nao e valido."
            password.isBlank() -> "A password e obrigatoria."
            password.length < PASSWORD_MINIMUM_LENGTH ->
                "A password deve ter pelo menos $PASSWORD_MINIMUM_LENGTH caracteres."
            !password.matches(passwordRegex) ->
                "A password deve ter pelo menos uma minuscula, uma maiuscula e um numero."
            else -> null
        }
    }
}
