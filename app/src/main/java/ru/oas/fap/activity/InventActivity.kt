package ru.oas.fap.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import ru.oas.fap.FAPActivityContract
import ru.oas.fap.R

class InventActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private val getNameFAP = registerForActivityResult(FAPActivityContract()) { result ->
        onFAPResult(result)
    }
//    private val tag = "myLogs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invent)
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val btnRemains = findViewById<Button>(R.id.btnRemains)
        btnRemains.setOnClickListener { loadRemains() }
        val btnSaveFTP = findViewById<Button>(R.id.btnSaveFTP)
        btnSaveFTP.setOnClickListener { saveInvent() }
        val btnDoc = findViewById<TextView>(R.id.btnDoc)
        btnDoc.setOnClickListener { openDoc() }
        val btnReport = findViewById<TextView>(R.id.btnReport)
        btnReport.setOnClickListener { openReport() }
        val tvName = findViewById<TextView>(R.id.tvName)
        if (prefs.contains("nameFAP")) {
            tvName.text = prefs.getString("nameFAP", "")
        } else {
            tvName.text = ""
        }
//        Log.d(tag, "onCreate (восстанавливаю) - " + tvName.text.toString())
        setStateButton(tvName.text.toString())
    }

    override fun onPause() {
        val tvName = findViewById<TextView>(R.id.tvName)
        val editor = prefs.edit()
        val nameFAP = tvName.text.toString()
        editor.putString("nameFAP", nameFAP).apply()
//        Log.d(tag, "onPause (сохраняю) - $nameFAP")
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        val tvName = findViewById<TextView>(R.id.tvName)
        if (prefs.contains("nameFAP")) {
            tvName.text = prefs.getString("nameFAP", "")
        } else {
            tvName.text = ""
        }
//        Log.d(tag, "onResume (восстанавливаю) - " + tvName.text.toString())
        setStateButton(tvName.text.toString())
    }

    private fun loadRemains() {
        getNameFAP.launch(0)
    }

    private fun onFAPResult(nameFAP: String) {
//        Log.d(tag, "onFAPResult - $nameFAP")
        if (nameFAP != "Back") {
            val tvName = findViewById<TextView>(R.id.tvName)
            tvName.text = nameFAP
//            Log.d(tag, "onFAPResult - " + tvName.text.toString())
            setStateButton(tvName.text.toString())
            val editor = prefs.edit()
            editor.putString("nameFAP", nameFAP).apply()
        }
    }

    private fun setStateButton(nameFAP: String) {
        val state = (nameFAP != "")
        val btnDoc = findViewById<TextView>(R.id.btnDoc)
        btnDoc.isEnabled = state
        btnDoc.isClickable = state
        val btnReport = findViewById<TextView>(R.id.btnReport)
        btnReport.isEnabled = state
        btnReport.isClickable = state
        val btnSaveFTP = findViewById<Button>(R.id.btnSaveFTP)
        btnSaveFTP.isEnabled = state
        btnSaveFTP.isClickable = state
    }

    private fun openDoc() {
        val intent = Intent(this@InventActivity, DocumentInventActivity::class.java)
        intent.putExtra("documentNumber", 1)
        startActivity(intent)
    }

    private fun openReport() {
        val intent = Intent(this@InventActivity, ReportActivity::class.java)
        startActivity(intent)
    }

    private fun saveInvent() {
        val intent = Intent(this@InventActivity, SaveInventActivity::class.java)
        startActivity(intent)
    }

}