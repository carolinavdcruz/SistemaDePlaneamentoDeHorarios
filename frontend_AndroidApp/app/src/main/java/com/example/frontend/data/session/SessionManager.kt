package com.example.frontend.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.frontend.data.model.OwnerType

// Guarda o id e role do utilizador logado em SharedPreferences.
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sph_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID   = "user_id"
        private const val KEY_USER_ROLE = "user_role"  // "Student" ou "Teacher"
    }

    fun saveSession(userId: Int, role: OwnerType) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USER_ROLE, role.name)
            .apply()
    }

    fun getUserId(): Int   = prefs.getInt(KEY_USER_ID, -1)
    fun getUserRole(): OwnerType?{
        val value = prefs.getString(KEY_USER_ROLE, null) ?: return null
        return OwnerType.entries.find { it.name == value }
    }

    fun isLoggedIn(): Boolean = getUserId() != -1

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}