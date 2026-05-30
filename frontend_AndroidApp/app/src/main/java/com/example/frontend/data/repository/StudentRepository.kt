package com.example.frontend.data.repository

import android.util.Log
import com.example.frontend.data.local.dao.StudentDao
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.remote.api.StudentApi
import com.example.frontend.data.remote.dto.StudentRequest

class StudentRepository(
    private val dao: StudentDao,
    private val api: StudentApi
) {

    private val tag = "StudentRepository"

    suspend fun insert(student: StudentEntity): Int {
        try {
            val remote = api.register(
                StudentRequest(
                    name = student.name,
                    email = student.email,
                    password = student.password
                )
            )
            dao.insert(
                StudentEntity(
                    id = remote.id,
                    teacherId = remote.teacherId,
                    name = remote.name,
                    email = remote.email,
                    password = remote.password,
                    maxDailySessions = student.maxDailySessions
                )
            )
            return remote.id
        } catch (e: Exception) {
            Log.e(tag, "Error sending student to API: ${e.message}")
            throw e
        }
    }

    suspend fun update(student: StudentEntity) {
        dao.update(student)
    }

    suspend fun delete(student: StudentEntity) {
        dao.delete(student)
    }

    suspend fun getAll(): List<StudentEntity> {
        return dao.getAll()
    }

    suspend fun getById(id: Int): StudentEntity? {
        return dao.getById(id)
    }

    suspend fun getByEmail(email: String): StudentEntity? {
        return try {
            val remote = api.getAll()
            val entities = remote.map {
                StudentEntity(
                    id = it.id,
                    teacherId = it.teacherId,
                    name = it.name,
                    email = it.email,
                    password = it.password,
                    maxDailySessions = 1
                )
            }

            dao.deleteAll()
            entities.forEach { dao.insert(it) }

            dao.getByEmail(email)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao procurar aluno por email via API: ${e.message}")
            dao.getByEmail(email)
        }
    }

    suspend fun getByTeacherId(teacherId: Int): List<StudentEntity> {
        return try {
            val remote = api.getStudentsByTeacher(teacherId)
            val entities = remote.map {
                StudentEntity(
                    id = it.id,
                    name = it.name,
                    email = it.email,
                    password = it.password,
                    teacherId = it.teacherId,
                    maxDailySessions = 1
                )
            }
            dao.deleteByTeacherId(teacherId)
            entities.forEach { dao.insert(it) }
            entities
        } catch (e: Exception) {
            Log.e(tag, "Erro ao buscar alunos do professor na API: ${e.message}")
            dao.getByTeacherId(teacherId)
        }
    }

    suspend fun assignTeacherToStudent(studentId: Int, teacherId: Int) {
        try {
            api.assignTeacher(studentId, teacherId)
            dao.assignTeacherToStudent(studentId, teacherId)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao associar professor ao aluno na API: ${e.message}")
            throw e
        }
    }

    suspend fun unassignTeacherFromStudent(studentId: Int) {
        try {
            api.unassignTeacher(studentId)
            dao.unassignTeacherFromStudent(studentId)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao desassociar professor do aluno na API: ${e.message}")
        }
    }
}
