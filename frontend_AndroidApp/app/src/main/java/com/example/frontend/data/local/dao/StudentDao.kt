package com.example.frontend.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.frontend.data.local.entity.StudentEntity

@Dao
interface StudentDao{

    // funções suspende porque permite que as funções corram em corrotinas e não bloqueiem a UI

    @Insert
    suspend fun insert(student: StudentEntity)

    @Update
    suspend fun update(student: StudentEntity)

    @Delete
    suspend fun delete(student: StudentEntity)

    @Query("SELECT * FROM student WHERE id = :id")
    suspend fun getById(id: Int): StudentEntity?

    @Query("SELECT * FROM student")
    suspend fun getAll(): List<StudentEntity>
}