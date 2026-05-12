package com.example.frontend.data.repository

import android.util.Log
import com.example.frontend.data.local.dao.StudentDao
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.remote.api.StudentApi
import com.example.frontend.data.remote.api.StudentRequest

class StudentRepository(
    private val dao: StudentDao,
    private val api: StudentApi
) {

    private val tag = "StudentRepository"

    suspend fun insert(student: StudentEntity) {
        dao.insert(student)
        try {
            api.register(StudentRequest(name = student.name, email = student.email))
        } catch (e: Exception) {
            Log.e(tag, "Error sending student to API: ${e.message}")
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

    suspend fun getByTeacherId(teacherId: Int): List<StudentEntity>{
       return dao.getByTeacherId(teacherId)
    }

    suspend fun assignTeacherToStudent(studentId: Int, teacherId: Int){
        dao.assignTeacherToStudent(studentId, teacherId)

    }

    suspend fun unassignTeacherFromStudent(studentId: Int){
        dao.unassignTeacherFromStudent(studentId)
    }

}