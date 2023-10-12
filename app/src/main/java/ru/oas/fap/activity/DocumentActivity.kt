package ru.oas.fap.activity

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import ru.oas.fap.AllViewModel
import ru.oas.fap.BarcodeActivityContract
import ru.oas.fap.R
import ru.oas.fap.ScanListAdapter
import ru.oas.fap.databinding.ActivityDocumentBinding
import ru.oas.fap.extensions.toast
import ru.oas.fap.room.CountData
import ru.oas.fap.room.ScanData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private var mSGTIN: String = ""
    private var fCamera: String? = ""
    private var fScan: String? = ""
    private var mDocumentNumber: Int = 0
    private lateinit var mCurrentScan: ScanData
    private val tableScan = mutableListOf<String>()
    private lateinit var binding: ActivityDocumentBinding
    private lateinit var errSound: SoundPool
    private var soundId: Int = 0
    private var spLoaded = false
    private var partScan: Int = 0
    private var partAvailable: Int = 0
    private var partTotal: Int = 0

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
                    mAllViewModel.deleteBarcodeId(scan.id)
                    tableScan.clear()
                    tableScan.addAll(mAllViewModel.getSGTINfromDocument(mDocumentNumber))
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
        mAllViewModel.setNumDoc(mDocumentNumber)
        mAllViewModel.mAllScans.observe(this) { scans ->
            scans?.let { scanAdapter.setScans(it) }
        }
        tableScan.clear()
        tableScan.addAll(mAllViewModel.getSGTINfromDocument(mDocumentNumber))
        setLayoutCount()

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        errSound = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .setMaxStreams(10)
            .build()
        errSound.setOnLoadCompleteListener { _, _, status ->
            spLoaded = status == 0
        }
        soundId = errSound.load(this, R.raw.cat, 1)
    }

    override fun onResume() {
        super.onResume()
        tableScan.clear()
        tableScan.addAll(mAllViewModel.getSGTINfromDocument(mDocumentNumber))
        setLayoutCount()
        errSound.resume(soundId)
    }

    override fun onPause() {
        errSound.pause(soundId)
        super.onPause()
    }

    override fun onDestroy() {
        errSound.release()
        super.onDestroy()
    }

    private fun onScanner() {
        getBarcode.launch(fCamera!!.toInt())
    }

    private fun onScannerResult(codes: Array<String>?){
        if (codes == null) return
        mSGTIN = codes[1]
        if (codes[0] == "DATA_MATRIX") {
            if (mSGTIN[0] == '\u001D' || mSGTIN[0] == '\u00E8') {  // для QR-кода убираю 1-й служебный
                mSGTIN = mSGTIN.substring(1)
            }
            mSGTIN = mSGTIN.filterNot { it == '\u001D'}
        }
        partAvailable = checkInNomen(mSGTIN)
        if (partAvailable == 0) {
            soundPlay()
            toast("Данной номенклатуры нет на остатках")
        } else {
            if (partAvailable > 1) {
                partTotal = checkPartNomen(mSGTIN)
                queryPart()
            } else {
                partScan = 1
                handlerBarcode()
            }
        }
    }

    private fun handlerBarcode() {
        if (partAvailable - partScan < 0) {
            soundPlay()
            toast("Данной номенклатуры нехватает на остатках, в остатке $partAvailable частей")
        }
        tableScan.add(mSGTIN)
        val mCurrentNom = mAllViewModel.getNomenByCode(mSGTIN)
        val df = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "RU"))
        if (mCurrentNom != null) {
            mCurrentScan = ScanData(
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
        mAllViewModel.insertScan(mCurrentScan)
        setLayoutCount()
    }

    private fun setLayoutCount() {
        binding.matrixLayoutCount.text = tableScan.filter { it.length <= 13 }.size.toString()
        binding.transportLayoutCount.text = tableScan.filter { it.length > 13 }.size.toString()
    }

    private fun soundPlay(){
        if (spLoaded) {
            errSound.play(soundId, 1F, 1F, 0, 0, 1F)
        }
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

    // проверка скана в остатках
    private fun checkInNomen(scan: String): Int{
        val res = mAllViewModel.countAvailable(scan)
        return if ((res == null) || (res == 0)) {
            0
        } else res
    }

    // сколько всего частей в остатках
    private fun checkPartNomen(scan: String): Int{
        val res = mAllViewModel.countPart(scan)
        return if ((res == null) || (res == 0)) {
            0
        } else res
    }

    @SuppressLint("SetTextI18n")
    private fun queryPart() {
        val li = LayoutInflater.from(this)
        val partsView: View = li.inflate(R.layout.query_only_part, null)
        val mDialogBuilder = AlertDialog.Builder(this)
        mDialogBuilder.setView(partsView)
        val userInput = partsView.findViewById<View>(R.id.input_part) as EditText
        val avPart = partsView.findViewById<View>(R.id.available_part) as TextView
        avPart.text = "Доступно частей - $partAvailable из $partTotal"
        mDialogBuilder
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                val memPartScan = userInput.text.toString()
                if (memPartScan != "") {
                    partScan = memPartScan.toInt()
                    handlerBarcode()
                }
            }
            .setNegativeButton(
                "Отмена"
            ) { dialogInterface, _ -> dialogInterface.cancel() }
        val alertDialog: AlertDialog = mDialogBuilder.create()
        alertDialog.show()
    }

}
