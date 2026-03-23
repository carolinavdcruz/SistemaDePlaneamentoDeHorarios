package com.example.frontend.data.repository

import com.example.frontend.data.local.dao.StudentDao
import com.example.frontend.data.local.entity.StudentEntity

class StudentRepository(private val dao: StudentDao) {

    suspend fun insert(student: StudentEntity) {
        dao.insert(student)
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
}