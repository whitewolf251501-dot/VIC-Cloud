package com.velmora.vic

import android.app.Application
import android.util.Log

class VicApplication : Application() {

    companion object {
        const val TAG = "VicApplication"
        lateinit var instance: VicApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i(TAG, "Velmora Intelligence Commander (VIC) Mobile Node Initialized.")
    }
}
