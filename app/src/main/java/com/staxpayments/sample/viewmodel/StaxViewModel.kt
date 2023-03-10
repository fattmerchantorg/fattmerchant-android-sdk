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
    // Set the api key value by setting `staxApiKey` in your `local.properties` file
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
     */
    fun onInitialize() {
        log("Initializing...")

        /**
         * Pass in your required parameters. `context` and `application` are not stored, but
         * required for the initialization with our hardware. Rather than use static variables
         * as used in this example, Stax recommends running your initialization code in a
         * custom Application class. However, for this example, we delay initialization to show
         * how it all works.
         */
        Stax.initialize(
            context = MainApplication.context,
            application = MainApplication.application,
            apiKey = apiKey,
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
     */
    fun onSearchAndConnectToReaders(context: Context) {
        log("Searching for readers...")


        /**
         * `Stax.instance().getAvailableReaders` returns a list of readers that you can potentially
         * connect to. These readers are searched over Bluetooth and not connected to when running
         * `getAvailableReaders()`. To connect them, we'll need to run `Stax.instance().connectReader`
         */
        Stax.instance().getAvailableReaders { found ->
            // Format our readers for readability
            val readers = found.map { "${it.getName()} - ${it.getConnectionType()}" }.toTypedArray()
            log("Found readers: ${found.map { it.getName() }}")

            // Build an alert dialog to prompt the user to select a reader to connect to
            val dialog = AlertDialog.Builder(context).setItems(readers) { _, which ->
                val selected = found[which]
                log("Trying to connect to [${selected.getName()}]...")

                /**
                 * Passing in one of the readers that was found, we call `Stax.instance().connectReader`
                 * to initiate the Bluetooth connection to the hardware reader. Depending on if the
                 * connection is a success or fail determines which of the two callbacks are called.
                 */
                Stax.instance().connectReader(
                    mobileReader = selected,
                    onConnected = { connected ->
                        reader = connected
                        log("Connected to [${reader?.getName()}]!")
                    },
                    onFail = { errorMsg ->
                        log("Error connecting: $errorMsg")
                    }
                )
            }.create()
            dialog.show()
        }
    }

    /**
     * Performs a charge of $0.01 on the reader
     * TODO: Read value from text input
     */
    fun onPerformSaleWithReader() {
        // The Amount class is used for handling off-by-one errors, rounding, and more
        val amount = Amount(1)
        val request = TransactionRequest(amount)

        log("Attempting to charge ${amount.dollarsString()}")

        Stax.instance().apply {
            // Listen to transaction updates delivered by the Stax SDK
            transactionUpdateListener = object : TransactionUpdateListener {
                override fun onTransactionUpdate(transactionUpdate: TransactionUpdate) {
                    log("${transactionUpdate.value} | ${transactionUpdate.userFriendlyMessage}")
                }
            }

            // Listen to user-level notifications
            userNotificationListener = object : UserNotificationListener {
                override fun onUserNotification(userNotification: UserNotification) {
                    log("${userNotification.value} | ${userNotification.userFriendlyMessage}")
                }

                override fun onRawUserNotification(userNotification: String) {
                    log(userNotification)
                }
            }

            /**
             * To run a charge, you call the `Stax.instance().takeMobileReaderTransaction()` function.
             * The function takes in a [TransactionRequest], a completion handler, and an error handler.
             * The completion handler is called if the transaction gets a response from the mobile
             * reader. If there is a problem with either the hardware or the api during the function,
             * the error handler is called.
             */
            takeMobileReaderTransaction(
                request = request,
                onCompletion = { transaction ->
                    if (transaction.success) {
                        log("Successfully executed transaction")
                    } else {
                        log("Transaction declined")
                    }
                    lastTransaction = transaction
                },
                onError = {
                    log("Couldn't perform sale: ${it.message}. ${it.detail}")
                }
            )
        }
    }

    /**
     * Performs a pre auth of $0.01 on the reader
     * TODO: Read value from text input
     */
    fun onPerformAuthWithReader() {
        // The Amount class also supports floats for more human-readable values
        val amount = Amount(0.01)
        val request = TransactionRequest(amount)
        request.preauth = true

        log("Attempting to auth ${amount.dollarsString()}")

        /**
         * To run a Pre-Authorization, you call the `Stax.instance().takeMobileReaderTransaction()`
         * function, but set the request.preauth value to `true.
         */
        Stax.instance().takeMobileReaderTransaction(
            request = request,
            onCompletion = { transaction ->
                if (transaction.success) {
                    log("Successfully authorized transaction")
                } else {
                    log("Transaction declined")
                }
                lastTransaction = transaction
            },
            onError = {
                log("Couldn't perform auth: ${it.message}. ${it.detail}")
            }
        )
    }

    /**
     * Takes the last transaction as a pre-auth and attempts to capture it
     * TODO: Read value from text input
     */
    fun onCaptureLastAuth() {
        if (lastTransaction?.id == null) { return }

        val id = lastTransaction?.id!!
        val amount = Amount(0.01)

        log("Attempting to capture last auth...")

        /**
         * To capture a pre-authorized transaction, you call the `Stax.instance().capturePreAuthTransaction()`
         * function. The function takes in an ID, as well as an optional amount. If no amount is
         * passed in, the full pre-authorized value will be captured.
         */
        Stax.instance().capturePreAuthTransaction(
            transactionId = id,
            amount = amount,
            onCompletion = { transaction ->
                if (transaction.success) {
                    log("Successfully authorized transaction")
                } else {
                    log("Transaction declined")
                }
            },
            onError = {
                log("Couldn't perform capture: ${it.message}. ${it.detail}")
            }
        )
    }

    /**
     * Voids the previous transaction
     */
    fun onVoidLastTransaction() {
        if (lastTransaction?.id == null) { return }
        val id = lastTransaction?.id!!

        log("Attempting to void last transaction...")

        /**
         * Voiding the last transaction only requires the transaction id of the transaction you
         * would like to void.
         */
        Stax.instance().voidTransaction(
            transactionId = id,
            onCompletion = { transaction ->
                if (transaction.success) {
                    log("Successfully voided transaction")
                } else {
                    log("Transaction declined")
                }
            },
            onError = { log("Couldn't perform void: ${it.message}. ${it.detail}") }
        )
    }

    /**
     * Tokenize the test card
     */
    fun onTokenizeCard() {
        /**
         * Tokenizing a credit card does not use the hardware, but it is a helpful tool for
         * tokenizing cards for use with the API. To tokenize a credit card, you create a
         * [CreditCard] object, and pass it into the `Stax.instance().tokenize()` function.
         */
        val card = CreditCard(
            personName = "John Doe",            // "First Last" format
            cardNumber = "4111111111111111",    // A Test Credit Card number
            cardExp = "0530",                   // "MMYY" format
            addressZip = "55555",               // 5 digit zip code
            address1 = "123 Orange Avenue",     // Street address
            addressCity = "Orlando",            // City
            addressState = "FL",                // State code. NOT the fully qualified state name
        )
        Stax.instance().tokenize(
            creditCard = card,
            onCompletion = { paymentMethod ->
                log("Successfully tokenized credit card")
                log(paymentMethod.toString())
            },
            onError =  { log("Couldn't tokenize card: ${it.message}. ${it.detail}") }
        )
    }

    /**
     * Show reader details
     */
    fun onGetConnectedReaderDetails() {
        /**
         * You can get some of the connection details for the hardware reader by running the
         * `Stax.instance().getConnectedReader()` function. This allows you to read various
         * hardware details that may be helpful for debugging issues with the Stax support team
         */
        Stax.instance().getConnectedReader(
            onReaderFound = { reader ->
                if (reader != null) {
                    log("Connected Reader:")
                    log(reader.toString())
                } else {
                    log("There is no connected reader")
                }
            },
            onFail = { log(it.toString()) }
        )
    }

    /**
     * Disconnect the current reader
     */
    fun onDisconnectReader() {
        /**
         * You can disconnect the current reader by running the `Stax.instance().disconnectReader()`
         * function. In this example, we check if the reader is connected before trying to disconnect.
         */
        Stax.instance().apply {
            getConnectedReader(
                onReaderFound = { reader ->
                    if (reader != null) {
                        disconnectReader(
                            mobileReader = reader,
                            onDisconnected = { log("Reader disconnected") },
                            onFail = { log(it.toString()) }
                        )
                    } else {
                        log("There is no connected reader")
                    }
                },
                // Failed to get connected reader...
                onFail = { log(it.toString()) }
            )
        }
    }
}