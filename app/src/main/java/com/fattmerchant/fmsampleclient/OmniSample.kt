package com.fattmerchant.fmsampleclient

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree


class OmniSample: Application() {

    override fun onCreate() {
        super.onCreate()
//        System.setProperty("kotlinx.coroutines.debug", "on")

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}