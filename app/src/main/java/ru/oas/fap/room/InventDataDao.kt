package ru.oas.fap.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface InventDataDao {

    @Query("SELECT SGTIN from InventData where NumDoc = :numDoc and Barcode = :barcode")
    fun getAllCodes(numDoc: Int, barcode: String): LiveData<List<CodesData>>

    @Query("SELECT Barcode as Barcode, substr(Name, 1, 30) as Name, Part as Part, Price as Price, id as id FROM InventData where NumDoc = :numDoc")
    fun getAllScans(numDoc: Int): LiveData<List<CountData>>

    @Query("SELECT DateTime, NumDoc from InventData group by NumDoc")
    fun getAllDocs(): LiveData<List<DocumentData>>

    @Query("SELECT * from InventData order by NumDoc")
    suspend fun getAll(): List<InventData>?

    @Query("SELECT SGTIN FROM InventData where NumDoc = :numDoc")
    suspend fun getSGTINfromDocument(numDoc: Int): List<String>

    @Query("DELETE from InventData where NumDoc = :numDoc")
    suspend fun delDoc(numDoc: Int)

    @Query("DELETE from InventData where id = :id")
    suspend fun delBarcodeId(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventData: InventData)

}