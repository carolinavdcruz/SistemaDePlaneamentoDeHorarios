package com.example.frontend.data.repository

import com.example.frontend.data.local.dao.TeacherDao
import com.example.frontend.data.local.entity.TeacherEntity

class TeacherRepository(private val dao: TeacherDao) {

    suspend fun insert(teacher: TeacherEntity) {
        dao.insert(teacher)
    }

    suspend fun update(teacher: TeacherEntity) {
        dao.update(teacher)
    }

    suspend fun delete(teacher: TeacherEntity) {
        dao.delete(teacher)
    }

    suspend fun getAll(): List<TeacherEntity> {
        return dao.getAll()
    }

    suspend fun getById(id: Int): TeacherEntity? {
        return dao.getById(id)
    }
}
