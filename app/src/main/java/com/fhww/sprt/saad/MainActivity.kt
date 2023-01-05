package com.fhww.sprt.saad

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.onesignal.OneSignal

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        OneSignal.initWithContext(this@MainActivity)
        OneSignal.setAppId("717760a4-1976-464d-9990-7e266633f202")
    }
}