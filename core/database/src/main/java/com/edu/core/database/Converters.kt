package com.edu.core.database

import androidx.room.TypeConverter

private const val LIST_DELIMITER = "¦"

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(LIST_DELIMITER)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split(LIST_DELIMITER)
    }
}
