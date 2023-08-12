package ru.oas.fap

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import ru.oas.fap.room.CodesData
import ru.oas.fap.room.CountData
import ru.oas.fap.room.DocumentData
import ru.oas.fap.room.ScanData
import ru.oas.fap.room.ScanDataDao

class ScanRepository(private val scanDataDao: ScanDataDao) {

    var mNumDoc: Int = 0
    val mAllDocs: LiveData<List<DocumentData>> = scanDataDao.getAllDocs()

    fun getScans(numDoc: Int): LiveData<List<CountData>> = scanDataDao.getAllScans(numDoc)
    fun getCodes(numDoc: Int, barcode: String): LiveData<List<CodesData>> = scanDataDao.getAllCodes(numDoc, barcode)

    @WorkerThread
    suspend fun getSGTINfromDocument(numDoc: Int): List<String> =
        scanDataDao.getSGTINfromDocument(numDoc)

    @WorkerThread
    suspend fun insert(scanData: ScanData) {
        scanDataDao.insert(scanData)
    }

    @WorkerThread
    suspend fun deleteBarcodeId(id: Long) {
        scanDataDao.delBarcodeId(id)
    }

    @WorkerThread
    suspend fun deleteSGTIN(numDoc: Int, sgtin: String) {
        scanDataDao.delSGTIN(numDoc, sgtin)
    }

    @WorkerThread
    suspend fun getNumberDocument(): Int {
        return scanDataDao.getNumberDocument()
    }

    @WorkerThread
    suspend fun deleteDoc(numDoc: Int) {
        scanDataDao.delDoc(numDoc)
    }

    @WorkerThread
    suspend fun getAll(): List<ScanData>? {
        return scanDataDao.getAll()
    }

}