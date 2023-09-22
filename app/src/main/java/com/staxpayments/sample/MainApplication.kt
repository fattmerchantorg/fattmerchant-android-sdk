package com.staxpayments.sample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class MainApplication : Application() {

    /**
     * Generally, we shouldn't store context like this. However, because it's a small example,
     * and because we're using the application context and not the activity or fragment context,
     * it's fine for this exmaple
     */
    companion object {
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