package com.staxpayments.sample.viewmodel

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.staxpayments.android.InitParams
import com.staxpayments.android.Stax
import com.staxpayments.sdk.Environment
import com.staxpayments.sdk.TransactionUpdateListener
import com.staxpayments.sdk.UserNotificationListener
import com.staxpayments.sdk.data.Amount
import com.staxpayments.sdk.data.MobileReader
import com.staxpayments.sdk.data.TransactionRequest
import com.staxpayments.sdk.data.TransactionUpdate
import com.staxpayments.sdk.data.UserNotification
import com.staxpayments.api.models.CreditCard
import com.staxpayments.api.models.Transaction
import com.staxpayments.sample.BuildConfig
import com.staxpayments.sample.MainApplication
import com.staxpayments.sample.SignatureProvider
import com.staxpayments.sample.state.StaxUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StaxViewModel : ViewModel() {
    private val apiKey = BuildConfig.STAX_API_KEY
    private var reader: MobileReader? = null
    private var lastTransaction: Transaction? = null

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
     * Runs the main Stax.initialize() code
     * TODO: Add better docs once Omni -> Stax rebranding
     */
    fun onInitialize() {
        log("Initializing...")
        Stax.initialize(
            params = InitParams(
                MainApplication.context,
                MainApplication.application,
                apiKey,
                Environment.LIVE
            ),
            onCompletion = {
                log("Initialized!")
                Stax.instance().signatureProvider = SignatureProvider()
            },
            onError = {
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

        Stax.instance().getAvailableReaders { found ->
            val readers = found.map { "${it.getName()} - ${it.getConnectionType()}" }.toTypedArray()
            log("Found readers: ${found.map { it.getName() }}")

            AlertDialog.Builder(ctx)
                .setItems(readers) { _, which ->
                    val selected = found[which]
                    log("Trying to connect to [${selected.getName()}]")
                    Stax.instance().connectReader(selected, { connectedReader ->
                        this.reader = connectedReader
                        log("Connected to [${this.reader?.getName()}]")
                    }, { error ->
                        log("Error connecting: $error")
                    })
                }.create().show()
        }
    }

    /**
     * Performs a charge of $0.01 on the reader
     * TODO: Add better docs once Omni -> Stax rebranding
     * TODO: Read value from text input
     */
    fun onPerformSaleWithReader() {
        val amount = Amount(1)
        log("Attempting to charge ${amount.dollarsString()}")
        val request = TransactionRequest(amount)

        // Listen to transaction updates delivered by the Omni SDK
        Stax.instance().transactionUpdateListener = object : TransactionUpdateListener {
            override fun onTransactionUpdate(transactionUpdate: TransactionUpdate) {
                log("${transactionUpdate.value} | ${transactionUpdate.userFriendlyMessage}")
            }
        }

        Stax.instance().userNotificationListener = object : UserNotificationListener {
            override fun onUserNotification(userNotification: UserNotification) {
                log("${userNotification.value} | ${userNotification.userFriendlyMessage}")
            }

            override fun onRawUserNotification(userNotification: String) {
                log(userNotification)
            }
        }

        Stax.instance().takeMobileReaderTransaction(request, { transaction ->
            val msg = if (transaction.success == true) {
                "Successfully executed transaction"
            } else {
                "Transaction declined"
            }
            log(msg)

            lastTransaction = transaction
        }, {
            log("Couldn't perform sale: ${it.message}. ${it.detail}")
        })
    }

    /**
     * Performs a pre auth of $0.01 on the reader
     * TODO: Add better docs once Omni -> Stax rebranding
     * TODO: Read value from text input
     */
    fun onPerformAuthWithReader() {
        val amount = Amount(0.01)
        log("Attempting to auth ${amount.dollarsString()}")

        val request = TransactionRequest(amount)
        request.preauth = true
        Stax.instance().takeMobileReaderTransaction(request, { transaction ->

            val msg = if (transaction.success == true) {
                "Successfully authed transaction"
            } else {
                "Transaction declined"
            }

            log(msg)

            lastTransaction = transaction
        }, {
            log("Couldn't perform auth: ${it.message}. ${it.detail}")
        })
    }

    /**
     * Takes the last transaction as a pre-auth and attempts to capture it
     * TODO: Add better docs once Omni -> Stax rebranding
     * TODO: Read value from text input
     */
    fun onCaptureLastAuth() {
        if (lastTransaction?.id == null) { return }

        val transactionId = lastTransaction?.id!!

        val amount = Amount(0.01)
        log("Attempting to capture last auth")

        Stax.instance().capturePreauthTransaction(transactionId, amount, { transaction ->
            val msg = if (transaction.success == true) {
                "Successfully captured transaction"
            } else {
                "Transaction declined"
            }
            log(msg)
        }, {
            log("Couldn't perform capture: ${it.message}. ${it.detail}")
        })
    }

    /**
     * Takes the last transaction as a pre-auth and attempts to void it
     * TODO: Add better docs once Omni -> Stax rebranding
     */
    fun onVoidLastTransaction() {
        if (lastTransaction?.id == null) { return }
        val transactionId = lastTransaction?.id!!

        Stax.instance().voidTransaction(transactionId, { transaction ->
            val msg = if (transaction.success == true) {
                "Successfully voided transaction"
            } else {
                "Transaction declined"
            }
            log(msg)
        }, {
            log("Couldn't perform void: ${it.message}. ${it.detail}")
        })
    }

    /**
     * Tokenize the test card
     * TODO: Show building Credit Card instead of test card
     */
    fun onTokenizeCard() {
        Stax.instance().tokenize(CreditCard.testCreditCard(), { paymentMethod ->
            log("Successfully tokenized credit card")
            log(paymentMethod.toString())
        }, {
            log("Couldn't tokenize card: ${it.message}. ${it.detail}")
        })
    }

    /**
     * Show reader details
     * TODO: cleanup code
     */
    fun onGetConnectedReaderDetails() {
        Stax.instance().getConnectedReader({ connectedReader ->
            connectedReader?.let { reader ->
                log("Connected Reader:")
                log(reader.toString())
            } ?: log("There is no connected reader")
        }, { exception ->
            log(exception.toString())
        })
    }

    /**
     * Disconnect the current reader
     * TODO: Cleanup example code
     */
    fun onDisconnectReader() {
        Stax.instance().getConnectedReader({ connectedReader ->
            connectedReader?.let { reader ->
                Stax.instance().disconnectReader(reader, {
                    log("Reader disconnected")
                }, {
                    log(it.toString())
                })
            } ?: log("There is no connected reader")
        }, { exception ->
            log(exception.toString())
        })
    }
}