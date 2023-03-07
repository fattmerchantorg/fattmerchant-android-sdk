package com.staxpayments.sample.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel

class StaxViewModel : ViewModel() {
    fun onInitialize() {
        Log.d("Stax SDK", "Initializing...")
    }

    fun onSearchForReaders() {

    }

    fun onConnectToSelectedReader() {

    }

    fun onPerformSaleWithReader() {

    }

    fun onPerformAuthWithReader() {

    }

    fun onCaptureLastAuth() {

    }

    fun onVoidLastAuth() {

    }

    fun onVoidLastTransaction() {

    }

    fun onTokenizeCard() {

    }

    fun onGetConnectedReaderDetails() {

    }

    fun onDisconnectReader() {

    }
}