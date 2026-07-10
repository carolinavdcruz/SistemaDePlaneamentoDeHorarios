package com.example.frontend

import com.example.frontend.data.model.RegisterValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegisterValidatorTest {

    @Test
    fun `validate devolve erro quando nome esta vazio`() {
        val result = RegisterValidator.validate(
            name = "",
            email = "user@isel.pt",
            password = "Password1"
        )

        assertEquals("O nome e obrigatorio.", result)
    }

    @Test
    fun `validate devolve erro quando nome e demasiado curto`() {
        val result = RegisterValidator.validate(
            name = "A",
            email = "user@isel.pt",
            password = "Password1"
        )

        assertEquals("O nome deve ter pelo menos 2 caracteres.", result)
    }

    @Test
    fun `validate devolve erro quando email esta vazio`() {
        val result = RegisterValidator.validate(
            name = "User",
            email = "",
            password = "Password1"
        )

        assertEquals("O email e obrigatorio.", result)
    }

    @Test
    fun `validate devolve erro quando email e invalido`() {
        val result = RegisterValidator.validate(
            name = "User",
            email = "email-invalido",
            password = "Password1"
        )

        assertEquals("O email introduzido nao e valido.", result)
    }

    @Test
    fun `validate devolve erro quando password esta vazia`() {
        val result = RegisterValidator.validate(
            name = "User",
            email = "user@isel.pt",
            password = ""
        )

        assertEquals("A password e obrigatoria.", result)
    }

    @Test
    fun `validate devolve erro quando password e curta`() {
        val result = RegisterValidator.validate(
            name = "User",
            email = "user@isel.pt",
            password = "Pass1"
        )

        assertEquals("A password deve ter pelo menos 8 caracteres.", result)
    }

    @Test
    fun `validate devolve erro quando password nao tem maiuscula minuscula e numero`() {
        val result = RegisterValidator.validate(
            name = "User",
            email = "user@isel.pt",
            password = "password"
        )

        assertEquals(
            "A password deve ter pelo menos uma minuscula, uma maiuscula e um numero.",
            result
        )
    }

    @Test
    fun `validate com dados validos devolve null`() {
        val result = RegisterValidator.validate(
            name = "User",
            email = "user@isel.pt",
            password = "Password1"
        )

        assertNull(result)
    }
}