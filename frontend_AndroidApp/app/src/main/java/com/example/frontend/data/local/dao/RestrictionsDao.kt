package com.example.frontend.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.frontend.data.local.entity.RestrictionsEntity

@Dao
interface RestrictionsDao {

    @Insert
    suspend fun insert(restrictions: RestrictionsEntity)

    @Update
    suspend fun update(restrictions: RestrictionsEntity)

    @Delete
    suspend fun delete(restrictions: RestrictionsEntity)

    @Query("SELECT * FROM restrictions WHERE teacherId = :teacherId")
    suspend fun getByTeacherId(teacherId: Int): RestrictionsEntity?
}