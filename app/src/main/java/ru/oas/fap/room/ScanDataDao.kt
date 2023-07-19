package ru.oas.fap.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScanDataDao {

    @Query("SELECT SGTIN from ScanData where NumDoc = :numDoc and Barcode = :barcode")
    fun getAllCodes(numDoc: Int, barcode: String): LiveData<List<CodesData>>

    @Query("SELECT Barcode as Barcode, substr(Name, 1, 30) as Name, Part as Part, Price as Price, id as id FROM ScanData where NumDoc = :numDoc")
    fun getAllScans(numDoc: Int): LiveData<List<CountData>>

    @Query("SELECT DateTime, NumDoc from ScanData group by NumDoc")
    fun getAllDocs(): LiveData<List<DocumentData>>

    @Query("SELECT * from ScanData order by NumDoc")
    suspend fun getAll(): List<ScanData>?

    @Query("SELECT IFNULL(max(NumDoc), '0') + 1 FROM scanData")
    suspend fun getNumberDocument(): Int

    @Query("SELECT SGTIN FROM scanData where NumDoc = :numDoc")
    suspend fun getSGTINfromDocument(numDoc: Int): List<String>

    @Query("DELETE from ScanData where NumDoc = :numDoc")
    suspend fun delDoc(numDoc: Int)

    @Query("DELETE from ScanData where id = :id")
    suspend fun delBarcodeId(id: Long)

    @Query("DELETE from ScanData where NumDoc = :numDoc and SGTIN = :sgtin")
    suspend fun delSGTIN(numDoc: Int, sgtin: String)

    @Query("DELETE from ScanData where SGTIN = :sgtin")
    suspend fun delCodes(sgtin: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scanData: ScanData)

}