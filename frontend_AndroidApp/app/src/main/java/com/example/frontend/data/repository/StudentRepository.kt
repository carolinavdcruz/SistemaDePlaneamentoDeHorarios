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

    suspend fun insert(student: StudentEntity) {
        try {
            val remote = api.register(StudentRequest(name = student.name, email = student.email))
            dao.insert(
                StudentEntity(
                    id = remote.id,
                    teacherId = remote.teacherId,
                    name = remote.name,
                    email = remote.email,
                    maxDailySessions = student.maxDailySessions
                )
            )
        } catch (e: Exception) {
            Log.e(tag, "Error sending student to API: ${e.message}")
            dao.insert(student)
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
        return dao.getByEmail(email)
    }

    suspend fun getByTeacherId(teacherId: Int): List<StudentEntity> {
        return try {
            val remote = api.getStudentsByTeacher(teacherId)
            val entities = remote.map {
                StudentEntity(
                    id = it.id,
                    name = it.name,
                    email = it.email,
                    teacherId = it.teacherId,
                    maxDailySessions = 1
                )
            }
            entities.forEach { dao.insert(it) }
            entities
        } catch (e: Exception) {
            Log.e(tag, "Erro ao buscar alunos do professor na API: ${e.message}")
            dao.getByTeacherId(teacherId)
        }
    }

    suspend fun assignTeacherToStudent(studentId: Int, teacherId: Int) {
        dao.assignTeacherToStudent(studentId, teacherId)
        try {
            api.assignTeacher(studentId, teacherId)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao associar professor ao aluno na API: ${e.message}")
        }
    }

    suspend fun unassignTeacherFromStudent(studentId: Int) {
        dao.unassignTeacherFromStudent(studentId)
        try {
            api.unassignTeacher(studentId)
        } catch (e: Exception) {
            Log.e(tag, "Erro ao desassociar professor do aluno na API: ${e.message}")
        }
    }
}
