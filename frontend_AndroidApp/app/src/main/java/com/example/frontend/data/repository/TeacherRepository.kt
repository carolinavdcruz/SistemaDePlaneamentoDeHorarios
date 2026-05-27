package com.example.frontend.data.repository

import android.util.Log
import com.example.frontend.data.local.dao.TeacherDao
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.remote.api.TeacherApi
import com.example.frontend.data.remote.dto.TeacherRequest


class TeacherRepository(
    private val dao: TeacherDao,
    private val api: TeacherApi
) {

    private val tag = "TeacherRepository"

    suspend fun insert(teacher: TeacherEntity): Int {
        // primeiro insere no backend para obter o ID real
        return try {
            val backendId = api.register(
                TeacherRequest(name = teacher.name, email = teacher.email)
            )
            backendId
        } catch (e: Exception) {
            Log.e(tag, "Erro ao registar professor no backend: ${e.message}")
            // fallback: guarda só local
            dao.insert(teacher)
            teacher.id
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
