package com.example.frontend

import android.content.Context
import android.content.SharedPreferences
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.session.SessionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionManagerTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.getSharedPreferences("sph_session", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.clear() } returns editor

        sessionManager = SessionManager(context)
    }

    @Test
    fun `saveSession guarda userId e role`() {
        sessionManager.saveSession(7, OwnerType.TEACHER)

        verify(exactly = 1) { editor.putInt("user_id", 7) }
        verify(exactly = 1) { editor.putString("user_role", "TEACHER") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getUserId devolve valor guardado`() {
        every { prefs.getInt("user_id", -1) } returns 10

        val result = sessionManager.getUserId()

        assertEquals(10, result)
    }

    @Test
    fun `getUserRole devolve role valida`() {
        every { prefs.getString("user_role", null) } returns "STUDENT"

        val result = sessionManager.getUserRole()

        assertEquals(OwnerType.STUDENT, result)
    }

    @Test
    fun `getUserRole devolve null quando nao existe role`() {
        every { prefs.getString("user_role", null) } returns null

        val result = sessionManager.getUserRole()

        assertNull(result)
    }

    @Test
    fun `getUserRole devolve null quando valor e invalido`() {
        every { prefs.getString("user_role", null) } returns "ROLE_ERRADA"

        val result = sessionManager.getUserRole()

        assertNull(result)
    }

    @Test
    fun `isLoggedIn devolve true quando userId existe`() {
        every { prefs.getInt("user_id", -1) } returns 3

        val result = sessionManager.isLoggedIn()

        assertTrue(result)
    }

    @Test
    fun `isLoggedIn devolve false quando userId e menos um`() {
        every { prefs.getInt("user_id", -1) } returns -1

        val result = sessionManager.isLoggedIn()

        assertFalse(result)
    }

    @Test
    fun `clearSession limpa preferencias`() {
        sessionManager.clearSession()

        verify(exactly = 1) { editor.clear() }
        verify(exactly = 1) { editor.apply() }
    }
}