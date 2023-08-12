package ru.oas.fap

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ru.oas.fap.activity.ListFAPActivity

class FAPActivityContract: ActivityResultContract<Int, String>() {

    override fun createIntent(context: Context, input: Int): Intent {
        return Intent(context, ListFAPActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        if (resultCode != Activity.RESULT_OK) return "Back"
        return intent?.getStringExtra("nameFAP").toString()
    }
}