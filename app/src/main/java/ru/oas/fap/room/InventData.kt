package ru.oas.fap.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "InventData", indices = [Index("NumDoc")])
data class InventData(
    @ColumnInfo(name = "DateTime") val dateTime: String,
    @ColumnInfo(name = "NumDoc") val numDoc: Int,
    @ColumnInfo(name = "Barcode") var barcode: String,  // полный код
    @ColumnInfo(name = "SGTIN") var sgtin: String,       // 31 символ
    @ColumnInfo(name = "Name") var nameNomen: String,
    @ColumnInfo(name = "Price") var price: Double,
    @ColumnInfo(name = "Part") var part: Int,
    @ColumnInfo(name = "NomId") var nomId: Long
){
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}