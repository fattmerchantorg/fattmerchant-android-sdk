package com.staxpayments.sample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log

// TODO: Enable Tap to Pay
// import com.creditcall.chipdnamobile.ChipDnaApplication

/**
 * Main application class.
 *
 * TODO: Enable Tap to Pay
 * To enable Tap to Pay (NFC), this class must extend ChipDnaApplication instead of Application.
 * This is required by NMI per MPoC requirements for the Tap to Pay solution.
 * You must also add the Cloud Commerce SDK AAR dependency (see CloudCommerceSDK module).
 * Reference: https://docs.nmi.com/docs/preparing-for-development-android
 *
 * Change:  class MainApplication : Application() {
 * To:      class MainApplication : ChipDnaApplication() {
 *
 * And implement the SDK initialization callbacks:
 *   override fun onSDKInitializationSuccess() {
 *       super.onSDKInitializationSuccess()
 *   }
 *   override fun onSDKInitializationFailed(errorMessage: String?) {
 *       super.onSDKInitializationFailed(errorMessage)
 *   }
 */
class MainApplication : Application() {

    companion object {
        private const val TAG = "MainApplication"

        /**
         * Generally, we shouldn't store context like this. However, because it's a small example,
         * and because we're using the application context and not the activity or fragment context,
         * it's fine for this example.
         */
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        application = this
    }
}
