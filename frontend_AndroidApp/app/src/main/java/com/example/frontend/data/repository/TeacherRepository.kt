package com.example.frontend.data.repository

import android.util.Log
import com.example.frontend.data.local.dao.TeacherDao
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.model.OwnerType
import com.example.frontend.data.remote.api.TeacherApi
import com.example.frontend.data.remote.dto.LoginRequest
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
                    password = teacher.password
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
                    password = ""
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
        val local = dao.getById(id)
        if (local != null) return local

        return try {
            val remoteTeachers = api.getAll()
            val found = remoteTeachers.firstOrNull { it.id == id }

            if (found != null) {
                val entity = TeacherEntity(
                    id = found.id,
                    name = found.name,
                    email = found.email,
                    password = ""
                )
                dao.insert(entity)
                entity
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Erro ao buscar professor por id na API: ${e.message}")
            null
        }
    }

    suspend fun getByEmail(email: String): TeacherEntity? {
        return dao.getByEmail(email)
    }

    suspend fun login(email: String, password: String): Pair<Int, OwnerType>? {
        return try {
            val response = api.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )

            val ownerType = OwnerType.valueOf(response.ownerType)
            response.userId to ownerType
        } catch (e: Exception) {
            Log.e(tag, "Erro ao fazer login via API: ${e.message}")
            null
        }
    }


}
