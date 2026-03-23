package com.example.frontend.data.repository

import com.example.frontend.data.local.dao.RestrictionsDao
import com.example.frontend.data.local.entity.RestrictionsEntity

class RestrictionsRepository(private val dao: RestrictionsDao) {

    suspend fun insert(restrictions: RestrictionsEntity) {
        dao.insert(restrictions)
    }

    suspend fun update(restrictions: RestrictionsEntity) {
        dao.update(restrictions)
    }

    suspend fun delete(restrictions: RestrictionsEntity) {
        dao.delete(restrictions)
    }

    suspend fun getByTeacherId(teacherId: Int): RestrictionsEntity? {
        return dao.getByTeacherId(teacherId)
    }
}
