package com.staxpayments.sample.viewmodel

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.fattmerchant.android.InitParams
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.Environment
import com.fattmerchant.omni.data.MobileReader
import com.staxpayments.sample.BuildConfig
import com.staxpayments.sample.MainApplication
import com.staxpayments.sample.SignatureProvider
import com.staxpayments.sample.state.StaxUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale

class StaxViewModel : ViewModel() {
    private val apiKey = BuildConfig.STAX_API_KEY
    private var reader: MobileReader? = null

    private val _uiState = MutableStateFlow(StaxUiState())
    val uiState: StateFlow<StaxUiState> = _uiState.asStateFlow()

    /**
     * Creates a new message in the UI Logger
     */
    private fun log(str: String) {
        val date = SimpleDateFormat("hh:mm:ss", Locale.US).format(Date())
        val msg = "$date | $str"
        _uiState.update { state ->
            state.copy(logString = state.logString + "$msg\n")
        }
    }

    /**
     * Runs the main Omni.initialize() code
     * TODO: Add better docs once Omni -> Stax rebranding
     */
    fun onInitialize() {
        log("Initializing...")
        Omni.initialize(
            params = InitParams(
                MainApplication.context,
                MainApplication.application,
                apiKey,
                Environment.LIVE
            ),
            completion = {
                log("Initialized!")
                Omni.shared()?.signatureProvider = SignatureProvider()
            },
            error = {
                Log.d("Stax SDK", "Fail to initialize...")
                Log.e("Stax SDK", it.toString())
                log("${it.message}. ${it.detail}")
            }
        )
    }

    /**
     * Searches for readers over BLE, shows an alert dialog, and connects to it
     * TODO: Add better docs once Omni -> Stax rebranding
     */
    fun onSearchAndConnectToReaders(ctx: Context) {
        log("Searching for readers...")
        Omni.shared()?.getAvailableReaders { found ->
            val readers = found.map { "${it.getName()} - ${it.getConnectionType()}" }.toTypedArray()
            log("Found readers: ${found.map { it.getName() }}")

            AlertDialog.Builder(ctx)
                .setItems(readers) { _, which ->
                    val selected = found[which]
                    log("Trying to connect to [${selected.getName()}]")
                    Omni.shared()?.connectReader(selected, { connectedReader ->
                        this.reader = connectedReader
                        log("Connected to [${this.reader?.getName()}]")
                    }, { error ->
                        log("Error connecting: $error")
                    })
                }.create().show()
        }
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