package ru.oas.fap.activity

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import org.json.JSONObject
import ru.oas.fap.AllViewModel
import ru.oas.fap.R
import ru.oas.fap.extensions.addScanInvent
import java.io.BufferedInputStream
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.Arrays
import java.util.Locale
import java.util.stream.Collectors

class SaveInventActivity : AppCompatActivity() {

    private lateinit var mAllViewModel: AllViewModel
    private var h = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_invent)
        val tvInfoLoad = findViewById<View>(R.id.tvStatusL) as TextView
        val tvInfoUpload = findViewById<View>(R.id.tvStatusU) as TextView
        val btLoad = findViewById<View>(R.id.btnLoad) as Button
        btLoad.setOnClickListener { onLoad() }
        h = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0 -> {  // кнопка - надпись
                        btLoad.text = msg.obj.toString()
                    }

                    1 -> {  // сколько загружено
                        tvInfoLoad.text = msg.obj.toString()
                    }

                    2 -> {  // сколько выгружено
                        tvInfoUpload.text = msg.obj.toString()
                    }

                    3 -> {  // кнопка - надпись + отключение
                        btLoad.text = msg.obj.toString()
                        btLoad.isEnabled = false
                    }
                }
            }
        }
    }

    private fun onLoad() {
        val t = Thread {
            val ftpClient = FTPClient()
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val server = prefs.getString("et_preference_server", "").toString()
            val user = "faps"
            val pass = "Tw789QwZ"
            var msg: Message?
            var countUpload = 0
            try {
                ftpClient.connect(server, FTP.DEFAULT_PORT)
                ftpClient.login(user, pass)
                val reply = ftpClient.replyCode
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect()
                    msg = h.obtainMessage(
                        1,
                        "FTP сервер не принимает подключение. Код ответа - $reply"
                    )
                    h.sendMessage(msg)
                    return@Thread
                }
                msg = h.obtainMessage(
                    0,
                    "Идет обмен..."
                )
                h.sendMessage(msg)
                ftpClient.enterLocalPassiveMode()
// выгружаю инвентаризацию в файл
                val resultSave = onSaveCodesToJSON()
                if (resultSave) {
// передаю файлы
                    val filesArray: Array<File> = filesDir.listFiles { _, filename ->
                        filename.lowercase(Locale.getDefault()).endsWith(".json")
                    } as Array<File>
                    for (fileIn in filesArray) {
                        val fi = fileIn.name
                        val fis: InputStream = BufferedInputStream(FileInputStream(fileIn))
                        val res = ftpClient.storeFile(fi, fis)
                        if (res) {
                            // получаю список файлов
                            val ftpFiles = Arrays.stream(ftpClient.listFiles(
                                ""
                            ) { file ->
                                file.isFile
                            })
                                .map { obj: FTPFile -> obj.name }
                                .collect(Collectors.toList())
                            val resultSearch = ftpFiles.indexOf(fi)
                            if (resultSearch != -1) {
                                countUpload += 1
                                mAllViewModel.deleteDocInvent(1)
                                fileIn.delete()
                            }
                        }
                    }
                }
                msg = if (countUpload > 0) {
                    h.obtainMessage(
                        2,
                        "Выгружен файл инвентаризации"
                    )
                } else {
                    h.obtainMessage(
                        2,
                        ""
                    )
                }
                h.sendMessage(msg)
            } catch (ex: IOException) {
                msg = h.obtainMessage(
                    1,
                    "Ошибка при обмене"
                )
                h.sendMessage(msg)
                ex.printStackTrace()
                return@Thread
            }
            try {
                if (ftpClient.isConnected) {
                    ftpClient.logout()
                    ftpClient.disconnect()
                }
            } catch (ex: IOException) {
                msg = h.obtainMessage(
                    1,
                    "Ошибка при закрытии соединения"
                )
                h.sendMessage(msg)
                ex.printStackTrace()
                return@Thread
            }
            msg = h.obtainMessage(
                3,
                "Обмен завершен!"
            )
            h.sendMessage(msg)
        }
        t.start()
    }

    private fun onSaveCodesToJSON(): Boolean {
        val scans = mutableListOf<JSONObject>()
        mAllViewModel = ViewModelProvider(this)[AllViewModel::class.java]
        val nameFileRemains = mAllViewModel.nameFileRemains().drop(4).dropLast(4)
        val all = mAllViewModel.getAllInvent()
        if (all!!.isNotEmpty()) {
            for (it in all) {
                scans.add(addScanInvent(it))
            }
            if (writeJsonInvent(scans.toString(), nameFileRemains)) {
//                mAllViewModel.deleteDocInvent(1)
                return true
            }
        }
        return false
    }

    private fun writeJsonInvent(jsonString: String, numMD: String): Boolean {
        return try {
            val fileName = createNameFileInvent(numMD)
            val fileWrite = File(filesDir, fileName)
            val outputStream: OutputStream = fileWrite.outputStream()
            val bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
            bufferedWriter.write(jsonString)
            bufferedWriter.flush()
            bufferedWriter.close()
            outputStream.close()
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun createNameFileInvent(numDoc: String): String {
        return "$numDoc.json"
    }

}