package com.example.frontend.data.local.database

import androidx.room.TypeConverter
import com.example.frontend.data.model.OwnerType

class OwnerTypeConverter {

    @TypeConverter
    fun fromOwnerType(value: OwnerType): String = value.name

    @TypeConverter
    fun toOwnerType(value: String): OwnerType = OwnerType.valueOf(value)
}