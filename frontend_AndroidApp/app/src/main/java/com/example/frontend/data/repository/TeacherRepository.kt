package com.example.frontend.data.repository

import android.util.Log
import com.example.frontend.data.local.dao.TeacherDao
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.remote.api.TeacherApi
import com.example.frontend.data.remote.api.TeacherRequest


class TeacherRepository(
    private val dao: TeacherDao,
    private val api: TeacherApi
) {

    private val tag = "TeacherRepository"

    suspend fun insert(teacher: TeacherEntity) {
        // guarda sempre localmente
        dao.insert(teacher)
        // envia para API
        try {
            api.register(TeacherRequest(name = teacher.name, email = teacher.email))
        } catch (e: Exception) {
            Log.e(tag, "Error sending teacher to API: ${e.message}")
        }
    }

    suspend fun update(teacher: TeacherEntity) {
        dao.update(teacher)
    }

    suspend fun delete(teacher: TeacherEntity) {
        dao.delete(teacher)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun getAll(): List<TeacherEntity> {
        return dao.getAll()
    }

    suspend fun getById(id: Int): TeacherEntity? {
        return dao.getById(id)
    }

    suspend fun getByEmail(email: String): TeacherEntity? {
        return dao.getByEmail(email)
    }

}
