package ru.oas.fap.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.oas.fap.AllViewModel
import ru.oas.fap.DocListAdapter
import ru.oas.fap.R
import ru.oas.fap.databinding.ActivityMainBinding
import ru.oas.fap.room.DocumentData

class MainActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private var fCamera: String? = ""
    private lateinit var binding: ActivityMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerPermissionListener()
        checkCameraPermission()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val docRecyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val docListAdapter = DocListAdapter(this)
        docRecyclerView.adapter = docListAdapter
        docRecyclerView.layoutManager = LinearLayoutManager(this)

        val onDocClickListener = object : DocListAdapter.OnDocClickListener {
            override fun onDocClick(docs: DocumentData, del: Boolean) = when {
                del -> {
                    mAllViewModel.deleteDoc(docs.numDoc)
                    Toast.makeText(this@MainActivity, "delete " + docs.dateTime, Toast.LENGTH_SHORT)
                        .show()
                }

                else -> {
                    val intent = Intent(this@MainActivity, DocumentActivity::class.java)
                    intent.putExtra("documentNumber", docs.numDoc)
                    startActivity(intent)
                }
            }
        }
        docListAdapter.docAdapter(onDocClickListener)

        // беру новую или сущ. ViewModel
        mAllViewModel = ViewModelProvider(this)[AllViewModel::class.java]

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mAllViewModel.mAllDocs.observe(this) { docs ->
            // Update the cached copy of the words in the adapter.
            docs?.let { docListAdapter.setDocs(it) }
        }

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { onDocument() }
        val ftp = findViewById<FloatingActionButton>(R.id.ftp)
        ftp.setOnClickListener { onUploadFTP() }
        val inv = findViewById<FloatingActionButton>(R.id.inv)
        inv.setOnClickListener { onInvent() }
        val count = mAllViewModel.countNomen().toString()
        binding.countNomen.text = "номенклатур в остатках - $count"
    }

    private fun checkCameraPermission(){
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            -> {
                Toast.makeText(this,"Камера работает", Toast.LENGTH_LONG).show()
            }
            else -> {
                pLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }
        }
    }

    private fun registerPermissionListener(){
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()){
            if(it[Manifest.permission.CAMERA] == true){
                Toast.makeText(this,"Камера работает", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this,"Разрешение отклонено", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        fCamera = prefs.getString("reply", "0")
        if (fCamera == "reply") {
            fCamera = "0"
        }
        if (fCamera!!.toInt() == 2) {
            binding.fab.hide()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                onSettings()
                true
            }

            R.id.action_about -> {
                onAbout()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private fun onSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun onDocument() {
        val intent = Intent(this@MainActivity, DocumentActivity::class.java)
        val numDoc = mAllViewModel.getNumberDocument()
        intent.putExtra("documentNumber", numDoc)
        startActivity(intent)
    }

    private fun onUploadFTP() {
        val intent = Intent(this@MainActivity, LoadActivity::class.java)
        startActivity(intent)
    }

    private fun onInvent() {
        val intent = Intent(this@MainActivity, InventActivity::class.java)
        startActivity(intent)
    }

    // LeftScan = 27  Scan = 301  RightScan = 80
    // Esc = 111                  Ent = 66
    // F1 = 131                   F2 = 132
    // BS = 67                    ., = 56
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == 66) {
            onDocument()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

}
