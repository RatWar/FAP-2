package ru.oas.fap

import androidx.annotation.WorkerThread
import ru.oas.fap.room.RemainsData
import ru.oas.fap.room.RemainsDataDao

class RemainsRepository(private val remainsDataDao: RemainsDataDao) {

    @WorkerThread
    suspend fun insert(remainsData: RemainsData) {
        remainsDataDao.insert(remainsData)
    }

    @WorkerThread
    suspend fun getRemainsByCode(barcode: String): RemainsData? {
        return remainsDataDao.getRemainsByCode(barcode)
    }

    @WorkerThread
    suspend fun countAvailable(barcode: String): Int? {
        return remainsDataDao.countAvailable(barcode)
    }

    @WorkerThread
    suspend fun countPart(barcode: String): Int? {
        return remainsDataDao.countPart(barcode)
    }

    @WorkerThread
    suspend fun nameFileRemains(): String {
        return remainsDataDao.nameFileRemains()
    }

    @WorkerThread
    suspend fun delRemains() {
        return remainsDataDao.delRemains()
    }

}