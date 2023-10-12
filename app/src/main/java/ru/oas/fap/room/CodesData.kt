package ru.oas.fap.room

import androidx.room.ColumnInfo

data class CodesData(
    @ColumnInfo(name = "SGTIN") val sgtin: String
)
