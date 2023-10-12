package ru.oas.fap.extensions

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

inline fun AppCompatActivity.toast(message: String, duration: () -> Int = { Toast.LENGTH_LONG }){
    Toast.makeText(this.applicationContext, message, duration()).show()
}