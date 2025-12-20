package com.fattmerchant.android.chipdna

import android.app.Activity
import android.util.Log
import com.creditcall.chipdnamobile.IRequestActivityListener
import java.lang.ref.WeakReference

/**
 * Handles NFC activity requests from the ChipDNA SDK for Tap to Pay transactions.
 *
 * This is a mandatory component for Tap to Pay on Android. The ChipDNA SDK requires an activity
 * context to display NFC prompts and handle contactless payment flows.
 *
 * The RequestActivityListener pattern ensures the SDK can request the current activity when needed
 * for NFC operations, without maintaining strong references that could cause memory leaks.
 *
 * @property activityProvider A lambda that provides the current Activity when requested by ChipDNA
 */
class RequestActivityDelegate(
    private val activityProvider: () -> Activity?
) : IRequestActivityListener {

    companion object {
        private const val TAG = "RequestActivityDelegate"
    }

    /**
     * Called by ChipDNA SDK when it needs the current Activity for NFC operations.
     *
     * This is invoked during Tap to Pay transactions to:
     * - Display NFC prompts to the user
     * - Handle contactless card interactions
     * - Manage transaction UI flow
     */
    override fun onRequestActivity() {
        val activity = activityProvider()
        if (activity == null) {
            Log.w(TAG, "No activity available for ChipDNA SDK request")
        } else {
            Log.d(TAG, "Providing activity to ChipDNA SDK: ${activity.javaClass.simpleName}")
        }
    }
}

/**
 * Convenience wrapper to hold a weak reference to an Activity for use with RequestActivityDelegate.
 *
 * Usage:
 * ```
 * class MainActivity : AppCompatActivity() {
 *     private val activityHolder = ActivityHolder()
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         activityHolder.activity = this
 *
 *         // Register with ChipDNA
 *         val delegate = RequestActivityDelegate { activityHolder.activity }
 *         ChipDnaMobile.getInstance().addRequestActivityListener(delegate)
 *     }
 *
 *     override fun onDestroy() {
 *         super.onDestroy()
 *         activityHolder.activity = null
 *     }
 * }
 * ```
 */
class ActivityHolder {
    private var activityRef: WeakReference<Activity>? = null

    var activity: Activity?
        get() = activityRef?.get()
        set(value) {
            activityRef = value?.let { WeakReference(it) }
        }
}
