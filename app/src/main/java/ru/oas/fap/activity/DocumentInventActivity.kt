package ru.oas.fap.activity

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import ru.oas.fap.AllViewModel
import ru.oas.fap.BarcodeActivityContract
import ru.oas.fap.R
import ru.oas.fap.ScanListAdapter
import ru.oas.fap.databinding.ActivityDocumentBinding
import ru.oas.fap.room.CountData
import ru.oas.fap.room.InventData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentInventActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private var mSGTIN: String = ""
    private var fCamera: String? = ""
    private var fScan: String? = ""
    private var mDocumentNumber: Int = 0
    private lateinit var mCurrentScanInvent: InventData
    private val tableScan = mutableListOf<String>()
    private lateinit var binding: ActivityDocumentBinding
    private var partScan: Int = 0  // кол-во частей
    private var fullScan: Int = 0  // кол-во целых уп.
    private var partAvailable: Int = 0  // доступно частей
    private var countPart: Int = 0  // делитель

    private val getBarcode = registerForActivityResult(BarcodeActivityContract()) { result ->
        onScannerResult(result)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.fabCamera.setOnClickListener { onScanner() }
        binding.fabSave.setOnClickListener { finish() }
        val scanRecyclerView = binding.recyclerScanList
        val scanAdapter = ScanListAdapter(this)
        scanRecyclerView.adapter = scanAdapter
        scanRecyclerView.layoutManager = LinearLayoutManager(this)

        val onScanClickListener = object : ScanListAdapter.OnScanClickListener {
            override fun onScanClick(scan: CountData, del: Boolean) {
                if (del) {
                    mAllViewModel.deleteBarcodeIdInvent(scan.id)
                    tableScan.clear()
                    tableScan.addAll(mAllViewModel.getSGTINfromDocumentInvent(mDocumentNumber))
                    setLayoutCount()
                }
            }
        }
        scanAdapter.scanAdapter(onScanClickListener)

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        fCamera = prefs.getString("reply", "0")
        fScan = prefs.getString("scan", "1")

        val intent = intent
        mDocumentNumber = intent.getIntExtra("documentNumber", 0)
        mAllViewModel = ViewModelProvider(this)[AllViewModel::class.java]
        mAllViewModel.setNumDocInvent(mDocumentNumber)
        mAllViewModel.mAllScansInvent.observe(this) { scans ->
            scans?.let { scanAdapter.setScans(it) }
        }
        tableScan.clear()
        tableScan.addAll(mAllViewModel.getSGTINfromDocumentInvent(mDocumentNumber))
        setLayoutCount()
    }

    override fun onResume() {
        super.onResume()
        tableScan.clear()
        tableScan.addAll(mAllViewModel.getSGTINfromDocumentInvent(mDocumentNumber))
        setLayoutCount()
    }

    // запуск сканирования телефоном
    private fun onScanner() {
        getBarcode.launch(fCamera!!.toInt())
    }

    private fun onScannerResult(codes: Array<String>?) {
        if (codes == null) return
        mSGTIN = codes[1]
        if (codes[0] == "DATA_MATRIX") {
            if (mSGTIN[0] == '\u001D' || mSGTIN[0] == '\u00E8') {  // для QR-кода убираю 1-й служебный
                mSGTIN = mSGTIN.substring(1)
            }
            mSGTIN = mSGTIN.filterNot { it == '\u001D'}
        }
        val barcode = mSGTIN
        fullScan = 0
        partScan = 0
        if (isKnownNomen(mSGTIN)) {              // есть в остатках
            partAvailable = checkInNomen(mSGTIN) // число доступных частей
            countPart = checkPartNomen(mSGTIN)   // делитель товара
            if (codes[0] == "EAN_13") {          // всегда запрашиваю кол-во для EAN_13
                queryPart(true, (countPart > 0))  // целые нужны, части определить
            } else {
                if (countPart > 0) {             // QR может делиться
                    queryPart(isFull = false, isPart = true)  // целые не нужны, только части
                } else {                         // QR кол-во = 1
                    partScan = 1
                    handlerBarcode()
                }
            }
        } else {                               // нет на остатках
            tableScan.add(mSGTIN)
            val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
            mCurrentScanInvent = InventData(
                df.format(Date()),
                mDocumentNumber,
                barcode.trim(),
                mSGTIN,
                barcode,
                0.00,
                1,
                0
            )
            mAllViewModel.insertScanInvent(mCurrentScanInvent)
            setLayoutCount()
        }
    }

    private fun handlerBarcode() {
        val mCurrentNom = mAllViewModel.getRemainsByCode(mSGTIN)
        val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
        while(fullScan > 0){
            tableScan.add(mSGTIN)
            if (mCurrentNom != null) {
                mCurrentScanInvent = InventData(
                    df.format(Date()),
                    mDocumentNumber,
                    mCurrentNom.barcode.trim(),
                    mSGTIN,
                    mCurrentNom.name,
                    mCurrentNom.price,
                    countPart,
                    mCurrentNom.id
                )
            }
            mAllViewModel.insertScanInvent(mCurrentScanInvent)
            setLayoutCount()
            fullScan--
        }
        if (partScan > 0) {
            tableScan.add(mSGTIN)
            if (mCurrentNom != null) {
                mCurrentScanInvent = InventData(
                    df.format(Date()),
                    mDocumentNumber,
                    mCurrentNom.barcode.trim(),
                    mSGTIN,
                    mCurrentNom.name,
                    mCurrentNom.price,
                    partScan,
                    mCurrentNom.id
                )
            }
            mAllViewModel.insertScanInvent(mCurrentScanInvent)
            setLayoutCount()
        }
    }

    // проверка скана в остатках
    private fun checkInNomen(scan: String): Int{
        val res = mAllViewModel.countAvailableRemains(scan)
        return if ((res == null) || (res == 0)) {
            0
        } else res
    }

    // сколько всего частей в остатках
    private fun checkPartNomen(scan: String): Int{
        val res = mAllViewModel.countPartRemains(scan)
        return if ((res == null) || (res == 0)) {
            0
        } else res
    }

    private fun isKnownNomen(scan: String): Boolean{
        val res = mAllViewModel.getRemainsByCode(scan)
        return res != null
    }

    @SuppressLint("SetTextI18n")
    private fun queryPart(isFull: Boolean, isPart: Boolean) {
        val li = LayoutInflater.from(this)
        val partsView: View = li.inflate(R.layout.query_part, null)
        val mDialogBuilder = AlertDialog.Builder(this)
        mDialogBuilder.setView(partsView)
        val avPart = partsView.findViewById<View>(R.id.available_part) as TextView
        val avFull = partsView.findViewById<View>(R.id.available_full) as TextView
        val inpPart = partsView.findViewById<View>(R.id.input_part) as EditText
        val inpFull = partsView.findViewById<View>(R.id.input_full) as EditText
        avFull.isVisible = isFull
        inpFull.isVisible = isFull
        avPart.isVisible = isPart
        inpPart.isVisible = isPart
        avPart.text = "Количество частей из $countPart"
        mDialogBuilder
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                if (isFull) {
                    val memFullScan = inpFull.text.toString()
                    if (memFullScan != "") {
                        fullScan = memFullScan.toInt()
                    }
                }
                if (isPart) {
                    val memPartScan = inpPart.text.toString()
                    if (memPartScan != "") {
                        partScan = memPartScan.toInt()
                    }
                }
                handlerBarcode()
            }
            .setNegativeButton(
                "Отмена"
            ) { dialogInterface, _ -> dialogInterface.cancel() }
        val alertDialog: AlertDialog = mDialogBuilder.create()
        alertDialog.show()
    }

    private fun setLayoutCount() {
        binding.matrixLayoutCount.text = tableScan.filter { it.length <= 13 }.size.toString()
        binding.transportLayoutCount.text = tableScan.filter { it.length > 13 }.size.toString()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_UP && fScan == "1") {
            onScanner()
            return true
        }
        if (event?.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && fScan == "-1") {
            onScanner()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}