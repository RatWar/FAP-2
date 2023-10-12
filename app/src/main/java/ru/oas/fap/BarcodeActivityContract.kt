package ru.oas.fap

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ru.oas.fap.activity.BarcodeActivity

class BarcodeActivityContract: ActivityResultContract<Int, Array<String>?>() {
    override fun createIntent(context: Context, input: Int): Intent {
        return Intent(context, BarcodeActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Array<String>? {
        if (resultCode != Activity.RESULT_OK) return null
        return arrayOf(intent?.getStringExtra("barcodeFormat").toString(), intent?.getStringExtra("scancode").toString())
    }
}