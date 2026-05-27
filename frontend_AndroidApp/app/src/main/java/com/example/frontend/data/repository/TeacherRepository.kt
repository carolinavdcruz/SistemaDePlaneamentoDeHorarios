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
        return try {
            val remote = api.register(TeacherRequest(name = teacher.name, email = teacher.email))
            dao.insert(
                TeacherEntity(
                    id = remote.id,
                    name = remote.name,
                    email = remote.email
                )
            )
            remote.id  // devolve o id real do backend
        } catch (e: Exception) {
            Log.e(tag, "Error sending teacher to API: ${e.message}")
            dao.insert(teacher)
            teacher.id  // fallback: devolve o id local
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
