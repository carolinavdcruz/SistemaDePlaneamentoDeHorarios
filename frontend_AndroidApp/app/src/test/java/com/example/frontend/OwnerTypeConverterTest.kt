package com.example.frontend

import com.example.frontend.data.local.database.OwnerTypeConverter
import com.example.frontend.data.model.OwnerType
import org.junit.Assert.assertEquals
import org.junit.Test

class OwnerTypeConverterTest {

    private val converter = OwnerTypeConverter()

    @Test
    fun `fromOwnerType converte STUDENT para string`() {
        val result = converter.fromOwnerType(OwnerType.STUDENT)

        assertEquals("STUDENT", result)
    }

    @Test
    fun `fromOwnerType converte TEACHER para string`() {
        val result = converter.fromOwnerType(OwnerType.TEACHER)

        assertEquals("TEACHER", result)
    }

    @Test
    fun `toOwnerType converte string STUDENT`() {
        val result = converter.toOwnerType("STUDENT")

        assertEquals(OwnerType.STUDENT, result)
    }

    @Test
    fun `toOwnerType converte string TEACHER`() {
        val result = converter.toOwnerType("TEACHER")

        assertEquals(OwnerType.TEACHER, result)
    }
}