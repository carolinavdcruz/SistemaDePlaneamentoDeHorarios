package com.example.frontend.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.frontend.data.local.entity.TeacherEntity

@Dao
interface TeacherDao {

    @Insert
    suspend fun insert(teacher: TeacherEntity)

    @Update
    suspend fun update(teacher: TeacherEntity)

    @Delete
    suspend fun delete(teacher: TeacherEntity)

    @Query("SELECT * FROM teacher WHERE id = :id")
    suspend fun getById(id: Int): TeacherEntity?

    @Query("SELECT * FROM teacher WHERE email = :email")
    suspend fun getByEmail(email: String): TeacherEntity?

    @Query("SELECT * FROM teacher")
    suspend fun getAll(): List<TeacherEntity>
}