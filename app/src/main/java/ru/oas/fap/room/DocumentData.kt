package ru.oas.fap.room

import androidx.room.ColumnInfo

data class DocumentData(
    @ColumnInfo(name = "DateTime") val dateTime: String,
    @ColumnInfo(name = "NumDoc") val numDoc: Int
)