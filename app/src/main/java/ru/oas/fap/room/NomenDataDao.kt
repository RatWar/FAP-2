package ru.oas.fap.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NomenDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nomenData: NomenData)

    @Query("SELECT * from NomenData where SGTIN = :barcode")
    suspend fun getNomenByCode(barcode: String): NomenData?

    @Query("SELECT count(*) from NomenData")
    suspend fun countNomen(): Int

    @Query("SELECT Available from NomenData where SGTIN = :barcode")
    suspend fun countAvailable(barcode: String): Int?

    @Query("SELECT Part from NomenData where SGTIN = :barcode")
    suspend fun countPart(barcode: String): Int?

    @Query("UPDATE NomenData set available = available + :available where id = :id")
    suspend fun updateAvailable(id: Long, available: Int)

    @Query("DELETE from NomenData")
    suspend fun delNomen()
}