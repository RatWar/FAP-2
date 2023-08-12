package ru.oas.fap.activity

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import ru.oas.fap.AllViewModel
import ru.oas.fap.R
import kotlin.math.roundToInt

class ReportActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private var countUnknown: Int = 0
    private lateinit var prefs: SharedPreferences
    private var sumRemains: Double = 0.0
//    private val tag = "myLogs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        val btnReport = findViewById<TextView>(R.id.button1)
        btnReport.setOnClickListener { onCloseReport() }
        val txtSumRemains = findViewById<TextView>(R.id.textView12)
        val txtSumInvent = findViewById<TextView>(R.id.textView22)
        val txtDiff = findViewById<TextView>(R.id.textView32)
        val txtUnknownText = findViewById<TextView>(R.id.textView41)
        val txtUnknown = findViewById<TextView>(R.id.textView42)
        mAllViewModel = ViewModelProvider(this)[AllViewModel::class.java]
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (prefs.contains("sumTotal")) {
            sumRemains = (prefs.getFloat("sumTotal", 0F).toDouble() * 100.0).roundToInt() / 100.0
            txtSumRemains.text = sumRemains.toString()
        } else {
            txtSumRemains.text = "нет остатков"
        }
        val sumInvent = (sumInvent() * 100.0).roundToInt() / 100.0
        txtSumInvent.text = sumInvent.toString()
        txtDiff.text = (sumRemains - sumInvent).toString()
        if (countUnknown > 0) {
            txtUnknown.text = countUnknown.toString()
        } else {
            txtUnknownText.isVisible = false
            txtUnknown.isVisible = false
        }
    }

    private fun sumInvent(): Double {
        var sum = 0.00
        var remPart: Int
        val all = mAllViewModel.getAllInvent()
        if (all!!.isNotEmpty()) {
            for (it in all) {
                if (it.nomId.toInt() == 0) {
                    countUnknown += 1
                    continue
                }
                val buf_part = it.part
//                Log.d(tag, "buf_part - $buf_part")
                if (buf_part == 0) {
                    val buf_price = it.price
//                    Log.d(tag, "buf_price - $buf_price")
                    sum += buf_price
//                    Log.d(tag, "sum - $sum")
                } else {
                    remPart = mAllViewModel.countPartRemains(it.sgtin)!!
//                    Log.d(tag, "remPart - $remPart")
                    when (remPart) {
                        0 -> {
                            remPart = 1
                        }
                    }
                    val buf = (it.part * 1.0 / remPart) * it.price
//                    Log.d(tag, "buf - $buf")
                    sum += buf
//                    Log.d(tag, "sum - $sum")
                }
            }
        }
        return sum
    }

    private fun onCloseReport() {
        finish()
    }
}