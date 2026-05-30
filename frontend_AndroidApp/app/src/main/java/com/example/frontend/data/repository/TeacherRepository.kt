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
            val remote = api.register(
                TeacherRequest(
                    name = teacher.name,
                    email = teacher.email,
                    password = teacher.password
                )
            )
            dao.insert(
                TeacherEntity(
                    id = remote.id,
                    name = remote.name,
                    email = remote.email,
                    password = remote.password
                )
            )
            remote.id
        } catch (e: Exception) {
            Log.e(tag, "Erro enviar professor para a API: ${e.message}")
            throw e
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
        return try {
            val remote = api.getAll()
            val entities = remote.map {
                TeacherEntity(
                    id = it.id,
                    name = it.name,
                    email = it.email,
                    password = it.password
                )
            }

            dao.deleteAll()
            entities.forEach { dao.insert(it) }

            entities
        } catch (e: Exception) {
            Log.e(tag, "Erro ao buscar professores da API: ${e.message}")
            dao.getAll()
        }
    }

    suspend fun getById(id: Int): TeacherEntity? {
        return dao.getById(id)
    }

    suspend fun getByEmail(email: String): TeacherEntity? {
        return try {
            val remote = api.getAll()
            val entities = remote.map {
                TeacherEntity(
                    id = it.id,
                    name = it.name,
                    email = it.email,
                    password = it.password
                )
            }

            dao.deleteAll()
            entities.forEach { dao.insert(it) }

            dao.getByEmail(email)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao procurar professor por email via API: ${e.message}")
            dao.getByEmail(email)
        }
    }

}
