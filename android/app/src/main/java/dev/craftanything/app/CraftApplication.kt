package dev.craftanything.app

import android.app.Application
import android.util.Log

class CraftApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("craft_core")
            Log.i(TAG, "Native library 'craft_core' loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native library 'craft_core' not found on device (will run in fallback mode): ${e.message}")
        }
    }

    companion object {
        private const val TAG = "CraftApplication"
    }
}
