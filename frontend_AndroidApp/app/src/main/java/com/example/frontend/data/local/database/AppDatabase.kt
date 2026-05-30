package com.example.frontend.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.frontend.data.local.dao.AvailabilityDao
import com.example.frontend.data.local.dao.RestrictionsDao
import com.example.frontend.data.local.dao.StudentDao
import com.example.frontend.data.local.dao.TeacherDao
import com.example.frontend.data.local.dao.TimeSlotDao
import com.example.frontend.data.local.entity.AvailabilityEntity
import com.example.frontend.data.local.entity.RestrictionsEntity
import com.example.frontend.data.local.entity.StudentEntity
import com.example.frontend.data.local.entity.TeacherEntity
import com.example.frontend.data.local.entity.TimeSlotEntity

@Database(
    entities = [
        TeacherEntity::class,
        StudentEntity::class,
        RestrictionsEntity::class,
        AvailabilityEntity::class,
        TimeSlotEntity::class
    ],
    version = 4, // subimos versao da db porque a estrutura das tabelas mudou
    exportSchema = false
)

@TypeConverters(OwnerTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun teacherDao(): TeacherDao
    abstract fun studentDao(): StudentDao
    abstract fun restrictionsDao(): RestrictionsDao
    abstract fun availabilityDao(): AvailabilityDao

    abstract fun timeSlotDao(): TimeSlotDao
}