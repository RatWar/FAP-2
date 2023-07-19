package ru.oas.fap.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemainsDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remainsData: RemainsData)

    @Query("SELECT * from RemainsData where SGTIN = :barcode")
    suspend fun getRemainsByCode(barcode: String): RemainsData?

    @Query("SELECT count(*) from RemainsData")
    suspend fun countRemains(): Int

    @Query("SELECT Available from RemainsData where SGTIN = :barcode")
    suspend fun countAvailable(barcode: String): Int?

    @Query("SELECT Part from RemainsData where SGTIN = :barcode")
    suspend fun countPart(barcode: String): Int?

    @Query("SELECT FileName from RemainsData limit 1")
    suspend fun nameFileRemains(): String

    @Query("UPDATE RemainsData set available = available + :available where id = :id")
    suspend fun updateAvailable(id: Long, available: Int)

    @Query("DELETE from RemainsData")
    suspend fun delRemains()

}