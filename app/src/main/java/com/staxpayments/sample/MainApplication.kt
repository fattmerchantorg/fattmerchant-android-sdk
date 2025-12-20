package com.staxpayments.sample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import com.creditcall.chipdnamobile.ChipDnaMobile
import com.creditcall.chipdnamobile.ParameterKeys
import com.creditcall.chipdnamobile.Parameters

/**
 * Main application class.
 * 
 * Note: According to NMI documentation, we should extend ChipDnaApplication.
 * However, that causes crashes due to Cloud Commerce SDK initialization issues.
 * As a workaround, we're manually initializing ChipDnaMobile here.
 */
class MainApplication : Application() {

    /**
     * Generally, we shouldn't store context like this. However, because it's a small example,
     * and because we're using the application context and not the activity or fragment context,
     * it's fine for this example
     */
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var application: Application
        
        private const val TAG = "MainApplication"
    }

    override fun onCreate() {
        super.onCreate()

        context = applicationContext
        application = this
        
        // Manually initialize ChipDnaMobile SDK
        // This is normally handled by ChipDnaApplication, but we're doing it manually
        // to avoid the CposApplication.onCreate() crash
        try {
            val params = Parameters().apply {
                add(ParameterKeys.Password, "password")
            }
            ChipDnaMobile.initialize(applicationContext, params)
            Log.d(TAG, "ChipDnaMobile initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ChipDnaMobile: ${e.message}", e)
        }
        
        Log.d(TAG, "MainApplication initialized")
    }
}