package ru.oas.fap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.oas.fap.room.CodesData
import ru.oas.fap.room.CountData
import ru.oas.fap.room.DocumentData
import ru.oas.fap.room.InventData
import ru.oas.fap.room.NomenData
import ru.oas.fap.room.RemainsData
import ru.oas.fap.room.ScanData
import ru.oas.fap.room.TSDDatabase

class AllViewModel(application: Application) : AndroidViewModel(application) {

    private val mScanRepository: ScanRepository
    private val mNomenRepository: NomenRepository
    private val mInventRepository: InventRepository
    private val mRemainsRepository: RemainsRepository
    var mAllCodes: LiveData<List<CodesData>>
    var mAllScans: LiveData<List<CountData>>
    val mAllDocs: LiveData<List<DocumentData>>
    private var mAllCodesInvent: LiveData<List<CodesData>>
    var mAllScansInvent: LiveData<List<CountData>>
    private val mAllDocsInvent: LiveData<List<DocumentData>>

    init {
        val scansDao = TSDDatabase.getDatabase(application).scanDataDao()
        val nomenDao = TSDDatabase.getDatabase(application).nomenDataDao()
        val inventDao = TSDDatabase.getDatabase(application).inventDataDao()
        val remainsDao = TSDDatabase.getDatabase(application).remainsDataDao()
        mScanRepository = ScanRepository(scansDao)
        mNomenRepository = NomenRepository(nomenDao)
        mInventRepository = InventRepository(inventDao)
        mRemainsRepository = RemainsRepository(remainsDao)
        mAllCodes = mScanRepository.getCodes(0, "")
        mAllScans = mScanRepository.getScans(0)
        mAllDocs = mScanRepository.mAllDocs
        mAllCodesInvent = mInventRepository.getCodes(0, "")
        mAllScansInvent = mInventRepository.getScans(0)
        mAllDocsInvent = mInventRepository.mAllDocs
    }

    // mScanRepository
    fun insertScan(scanData: ScanData) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.insert(scanData)
    }

    fun deleteBarcodeId(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.deleteBarcodeId(id)
    }

    fun deleteSGTIN(numDoc: Int, sgtin: String) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.deleteSGTIN(numDoc, sgtin)
    }

    fun deleteDoc(numDoc: Int) = viewModelScope.launch(Dispatchers.IO) {
        mScanRepository.deleteDoc(numDoc)
    }

    fun getNumberDocument(): Int {
        var res: Int
        runBlocking { res = mScanRepository.getNumberDocument() }
        return res
    }

    fun setNumDocAndBarcode(numDoc: Int, barcode: String){
        mScanRepository.mNumDoc = numDoc
        mAllCodes = mScanRepository.getCodes(numDoc, barcode)
    }

    fun setNumDoc(numDoc: Int){
        mScanRepository.mNumDoc = numDoc
        mAllScans = mScanRepository.getScans(numDoc)
    }


    fun getAll(): List<ScanData>? {
        var res: List<ScanData>?
        runBlocking { res = mScanRepository.getAll() }
        return res
    }

    fun getSGTINfromDocument(numDoc: Int): List<String> {
        var res: List<String>
        runBlocking { res = mScanRepository.getSGTINfromDocument(numDoc) }
        return res
    }

    // mNomenRepository
    fun insertNomenBlocking(nomenData: NomenData) {
        runBlocking { mNomenRepository.insert(nomenData) }
    }

    fun getNomenByCode(barcode: String): NomenData? {
        var res: NomenData?
        runBlocking {res = mNomenRepository.getNomenByCode(barcode) }
        return res
    }

    fun countNomen(): Int {
        var res: Int
        runBlocking { res = mNomenRepository.countNomen() }
        return res
    }

    fun countAvailable(barcode: String): Int? {
        var res: Int?
        runBlocking { res = mNomenRepository.countAvailable(barcode) }
        return res
    }

    fun countPart(barcode: String): Int? {
        var res: Int?
        runBlocking { res = mNomenRepository.countPart(barcode) }
        return res
    }

    fun updateAvailable(id: Long, available: Int) = viewModelScope.launch(Dispatchers.IO) {
        mNomenRepository.updateAvailable(id, available)
    }

    fun delNomen() {
        runBlocking { mNomenRepository.delNomen() }
    }

    // mInventRepository
    fun insertScanInvent(inventData: InventData) = viewModelScope.launch(Dispatchers.IO) {
        mInventRepository.insert(inventData)
    }

    fun deleteBarcodeIdInvent(id: Long) = viewModelScope.launch(Dispatchers.IO) {
        mInventRepository.deleteBarcodeId(id)
    }

    fun deleteDocInvent(numDoc: Int) = viewModelScope.launch(Dispatchers.IO) {
        mInventRepository.deleteDoc(numDoc)
    }

    fun setNumDocInvent(numDoc: Int){
        mInventRepository.mNumDoc = numDoc
        mAllScansInvent = mInventRepository.getScans(numDoc)
    }


    fun getAllInvent(): List<InventData>? {
        var res: List<InventData>?
        runBlocking { res = mInventRepository.getAll() }
        return res
    }

    fun getSGTINfromDocumentInvent(numDoc: Int): List<String> {
        var res: List<String>
        runBlocking { res = mInventRepository.getSGTINfromDocument(numDoc) }
        return res
    }

    // mRemainsRepository
    fun insertRemainsBlocking(remainsData: RemainsData) {
        runBlocking { mRemainsRepository.insert(remainsData) }
    }

    fun getRemainsByCode(barcode: String): RemainsData? {
        var res: RemainsData?
        runBlocking {res = mRemainsRepository.getRemainsByCode(barcode) }
        return res
    }

    fun countAvailableRemains(barcode: String): Int? {
        var res: Int?
        runBlocking { res = mRemainsRepository.countAvailable(barcode) }
        return res
    }

    fun countPartRemains(barcode: String): Int? {
        var res: Int?
        runBlocking { res = mRemainsRepository.countPart(barcode) }
        return res
    }

    fun nameFileRemains(): String {
        var res: String
        runBlocking { res = mRemainsRepository.nameFileRemains() }
        return res
    }

    fun delRemains() {
        runBlocking { mRemainsRepository.delRemains() }
    }

}
