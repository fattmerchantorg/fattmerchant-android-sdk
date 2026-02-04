package com.staxpayments.sample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.creditcall.chipdnamobile.ChipDnaApplication

/**
 * Main application class extending ChipDnaApplication as required by NMI.
 * 
 * Per NMI documentation: "To initialize the Payment Device SDK to allow Tap To Pay,
 * it is required that the integrating application must contain an application class
 * that extends ChipDnaApplication. This is due to MPoC requirements for the Tap To Pay solution."
 * 
 * Reference: https://docs.nmi.com/docs/preparing-for-development-android
 */
class MainApplication : ChipDnaApplication() {

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
        
        Log.d(TAG, "MainApplication initialized - waiting for Cloud Commerce SDK initialization")
    }

    /**
     * Called when the Cloud Commerce SDK initializes successfully.
     * This is required to be implemented and must call super.
     */
    override fun onSDKInitializationSuccess() {
        // Required to call super per NMI documentation
        super.onSDKInitializationSuccess()
        Log.d(TAG, "✅ Cloud Commerce SDK Initialization Success")
    }

    /**
     * Called when the Cloud Commerce SDK fails to initialize.
     * This is required to be implemented and must call super.
     */
    override fun onSDKInitializationFailed(errorMessage: String?) {
        // Required to call super per NMI documentation
        super.onSDKInitializationFailed(errorMessage)
        Log.e(TAG, "❌ Cloud Commerce SDK Initialization Failed: $errorMessage")
        Toast.makeText(this, "SDK Init Failed: $errorMessage", Toast.LENGTH_LONG).show()
    }
}
