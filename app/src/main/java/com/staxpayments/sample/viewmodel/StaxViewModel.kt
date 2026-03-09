package com.staxpayments.sample.viewmodel

import android.app.AlertDialog
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fattmerchant.android.InitParams
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.Environment
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.UsbAccessoryListener
import com.fattmerchant.omni.UserNotificationListener
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.TransactionUpdate
import com.fattmerchant.omni.data.UserNotification
import com.fattmerchant.omni.data.models.CreditCard
import com.fattmerchant.omni.data.models.Transaction
import com.staxpayments.sample.BuildConfig
import com.staxpayments.sample.MainApplication
import com.staxpayments.sample.SignatureProvider
import com.staxpayments.sample.state.StaxUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Enable Tap to Pay
// Uncomment the following imports when enabling Tap to Pay
// import com.fattmerchant.omni.data.ReaderType
// import com.fattmerchant.omni.data.TapToPayConfiguration
// import com.fattmerchant.omni.data.TapToPayReader

class StaxViewModel : ViewModel(), UsbAccessoryListener {

    companion object {
        private const val TAG = "StaxViewModel"

        // Replace with your Gateway API Key from the Gateway Control Panel's Security Keys page
        // For the chipdnatest flavor, NMI's pre-registered test credentials are used instead
        private const val APP_ID = "fattmerchantsample"

        // TODO: Enable Tap to Pay
        // Set the certificate fingerprint value. This is used by TTM to initialize transactions.
        // Generate with: keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android
        // If left null, the SDK will auto-extract the fingerprint from the app's signing certificate.
        // private const val CERTIFICATE_FINGERPRINT: String? = null
    }

    // Set the api key value by setting `staxApiKey` in your `local.properties` file
    private val apiKey = BuildConfig.STAX_API_KEY
    private var reader: MobileReader? = null
    private var lastTransaction: Transaction? = null
    private var currentActivity: android.app.Activity? = null

    /** Guards against multiple Omni.initialize() calls from the same ViewModel instance. */
    private var isOmniInitialized = false

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
     * Sets the current Activity for NFC operations
     */
    fun setActivity(activity: android.app.Activity?) {
        currentActivity = activity
    }

    /**
     * Gets an Ephemeral Token from the Stax API
     */
    private suspend fun getToken(): JSONObject {
        return withContext(Dispatchers.IO) {
            val url = URL("https://apiprod.fattlabs.com/ephemeral")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 5000
                readTimeout = 5000
            }

            try {
                val response = StringBuilder()
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                }
                return@withContext JSONObject(response.toString())
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * Runs the main Omni.initialize() code.
     *
     * Pass in your required parameters. `context` and `application` are not stored, but
     * required for the initialization with our hardware. Rather than use static variables
     * as used in this example, Stax recommends running your initialization code in a
     * custom Application class.
     */
    fun onInitialize() {
        if (isOmniInitialized) {
            log("Already initialized -- skipping duplicate init")
            return
        }
        isOmniInitialized = true
        log("Initializing...")

        // TODO: Enable Tap to Pay
        // When running the chipdnatest flavor, swap in NMI's pre-registered test credentials:
        // val resolvedApiKey = if (BuildConfig.IS_NMI_TEST_FLAVOR) BuildConfig.NMI_TEST_API_KEY else apiKey
        // val resolvedAppId = if (BuildConfig.IS_NMI_TEST_FLAVOR) BuildConfig.NMI_TEST_APP_ID else APP_ID

        val params = InitParams(
            MainApplication.context,
            MainApplication.application,
            apiKey,
            environment = Environment.LIVE,
            appId = APP_ID,

            // TODO: Enable Tap to Pay
            // Uncomment the following to enable Tap to Pay (NFC) transactions.
            // Make sure your app:
            //   1. Extends ChipDnaApplication (see MainApplication.kt)
            //   2. Includes the Cloud Commerce SDK AAR (see CloudCommerceSDK module)
            //   3. Has been registered with NMI (email taptopay-app-onboarding@nmi.com)
            //   4. Is signed with the keystore matching the certificate fingerprint shared with NMI
            //
            // tapToPayConfig = TapToPayConfiguration(
            //     enabled = true,
            //     allowExternalReaders = true,  // false = Tap to Pay only, true = hybrid mode
            //     certificateFingerprint = CERTIFICATE_FINGERPRINT,  // null = auto-extract from signing cert
            //     testMode = true  // true = MTF/sandbox, false = production
            // ),
            // sandBoxKey = true  // Set to true when using MTF Cloud Commerce SDK
        )

        Omni.initialize(
            params = params,
            completion = {
                log("Initialized!")
                Omni.shared()?.signatureProvider = SignatureProvider()

                // TODO: Enable Tap to Pay
                // Register the Activity provider for NFC operations. This is required before
                // connecting to a Tap to Pay reader. The SDK needs an active Activity to display
                // the NFC payment prompt.
                //
                // currentActivity?.let { activity ->
                //     Omni.shared()?.registerTapToPayActivityProvider { activity }
                //     log("Registered Activity delegate for Tap to Pay")
                // }
            },
            error = { exception ->
                log("There was an error initializing...")
                log("${exception.message}. ${exception.detail}")
            },
            usbListener = this
        )
    }

    /**
     * Runs Omni.initialize() with an ephemeral token
     */
    fun onEphemeralInitialize() {
        if (isOmniInitialized) {
            log("Already initialized -- skipping duplicate init")
            return
        }
        isOmniInitialized = true
        log("Initializing with token...")

        val listener = this
        viewModelScope.launch {
            val token = getToken().getString("token")

            val params = InitParams(
                MainApplication.context,
                MainApplication.application,
                token,

                // TODO: Enable Tap to Pay
                // tapToPayConfig = TapToPayConfiguration(
                //     enabled = true,
                //     allowExternalReaders = true,
                //     certificateFingerprint = CERTIFICATE_FINGERPRINT,
                //     testMode = true
                // ),
                // sandBoxKey = true
            )

            Omni.initialize(
                params = params,
                completion = {
                    log("Initialized with token!")
                    Omni.shared()?.signatureProvider = SignatureProvider()

                    // TODO: Enable Tap to Pay
                    // currentActivity?.let { activity ->
                    //     Omni.shared()?.registerTapToPayActivityProvider { activity }
                    // }
                },
                error = { exception ->
                    log("There was an error initializing with token...")
                    log("${exception.message}. ${exception.detail}")
                },
                usbListener = listener
            )
        }
    }

    // TODO: Enable Tap to Pay
    // Uncomment the following method to enable connecting to the Tap to Pay reader.
    // This connects a virtual NFC reader — no physical pairing is needed.
    //
    // fun connectToTapReader() {
    //     log("Connecting to Tap reader...")
    //
    //     // Check location permissions — required by NMI for Tap to Pay attestation
    //     val context = MainApplication.context
    //     val fineLocationGranted = ContextCompat.checkSelfPermission(
    //         context, Manifest.permission.ACCESS_FINE_LOCATION
    //     ) == PackageManager.PERMISSION_GRANTED
    //     val coarseLocationGranted = ContextCompat.checkSelfPermission(
    //         context, Manifest.permission.ACCESS_COARSE_LOCATION
    //     ) == PackageManager.PERMISSION_GRANTED
    //
    //     if (!fineLocationGranted || !coarseLocationGranted) {
    //         log("Location permissions not granted — NMI SDK will fail attestation")
    //         return
    //     }
    //
    //     // Register RequestActivityListener before connecting
    //     currentActivity?.let { activity ->
    //         Omni.shared()?.registerTapToPayActivityProvider { activity }
    //     } ?: run {
    //         log("No Activity set — call setActivity() before connecting to Tap reader")
    //         return
    //     }
    //
    //     val tapReader = TapToPayReader(testMode = true)  // false for production
    //
    //     Omni.shared()?.connectReader(
    //         mobileReader = tapReader,
    //         onConnected = { connectedReader ->
    //             reader = connectedReader
    //             log("Connected to Tap reader: ${connectedReader.getName()}")
    //         },
    //         onFail = { errorMsg ->
    //             log("Failed to connect to Tap reader: $errorMsg")
    //         }
    //     )
    // }

    /**
     * Searches for readers over BLE, shows an alert dialog, and connects to it
     */
    fun onSearchAndConnectToReaders(context: Context) {
        log("Searching for readers...")

        /**
         * `Omni.shared().getAvailableReaders` returns a list of readers that you can potentially
         * connect to. These readers are searched over Bluetooth and not connected to when running
         * `getAvailableReaders()`. To connect them, we'll need to run `Omni.shared().connectReader`
         */
        Omni.shared()?.getAvailableReaders { found ->
            val readers = found.map { "${it.getName()} - ${it.getConnectionType()}" }.toTypedArray()
            log("Found readers: ${found.map { it.getName() }}")

            val dialog = AlertDialog.Builder(context).setItems(readers) { _, which ->
                val selected = found[which]
                log("Trying to connect to [${selected.getName()}]...")

                Omni.shared()?.connectReader(
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
     * Performs a charge of $0.01 on the reader.
     */
    fun onPerformSaleWithReader() {
        val amount = Amount(1)
        val request = TransactionRequest(amount)

        log("Attempting to charge ${amount.dollarsString()}")

        Omni.shared()?.apply {
            transactionUpdateListener = object : TransactionUpdateListener {
                override fun onTransactionUpdate(transactionUpdate: TransactionUpdate) {
                    log("${transactionUpdate.value} | ${transactionUpdate.userFriendlyMessage}")
                }
            }

            userNotificationListener = object : UserNotificationListener {
                override fun onUserNotification(userNotification: UserNotification) {
                    log("${userNotification.value} | ${userNotification.userFriendlyMessage}")
                }

                override fun onRawUserNotification(userNotification: String) {
                    log(userNotification)
                }
            }

            // TODO: Enable Tap to Pay
            // To use Tap to Pay for transactions, pass readerType = ReaderType.TAP_TO_PAY
            // For hybrid mode (auto-select based on connected reader), use ReaderType.AUTO
            // For external readers only, use ReaderType.EXTERNAL_READER or omit the parameter
            //
            // takeMobileReaderTransaction(
            //     request = request,
            //     readerType = ReaderType.TAP_TO_PAY,  // or ReaderType.AUTO for hybrid
            //     completion = { ... },
            //     error = { ... }
            // )

            takeMobileReaderTransaction(
                request = request,
                completion = { transaction ->
                    if (transaction.success == true) {
                        log("Successfully executed transaction")
                    } else {
                        log("Transaction declined")
                    }
                    lastTransaction = transaction
                },
                error = {
                    log("Couldn't perform sale: ${it.message}. ${it.detail}")
                }
            )
        }
    }

    /**
     * Performs a pre auth of $0.01 on the reader
     */
    fun onPerformAuthWithReader() {
        val amount = Amount(0.01)
        val request = TransactionRequest(amount)
        request.preauth = true

        log("Attempting to auth ${amount.dollarsString()}")

        Omni.shared()?.takeMobileReaderTransaction(
            request = request,
            completion = { transaction ->
                if (transaction.success == true) {
                    log("Successfully authorized transaction")
                } else {
                    log("Transaction declined")
                }
                lastTransaction = transaction
            },
            error = {
                log("Couldn't perform auth: ${it.message}. ${it.detail}")
            }
        )
    }

    /**
     * Takes the last transaction as a pre-auth and attempts to capture it
     */
    fun onCaptureLastAuth() {
        if (lastTransaction?.id == null) { return }

        val id = lastTransaction?.id!!
        val amount = Amount(0.01)

        log("Attempting to capture last auth...")

        Omni.shared()?.capturePreauthTransaction(
            transactionId = id,
            amount = amount,
            completion = { transaction ->
                if (transaction.success == true) {
                    log("Successfully captured transaction")
                } else {
                    log("Transaction declined")
                }
            },
            error = {
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

        Omni.shared()?.voidTransaction(
            transactionId = id,
            completion = { transaction ->
                if (transaction.success == true) {
                    log("Successfully voided transaction")
                } else {
                    log("Transaction declined")
                }
            },
            error = {
                log("Couldn't perform void: ${it.message}. ${it.detail}")
            }
        )
    }

    /**
     * Tokenize the test card
     */
    fun onTokenizeCard() {
        val card = CreditCard(
            personName = "John Doe",
            cardNumber = "4111111111111111",
            cardExp = "0530",
            addressZip = "55555",
            address1 = "123 Orange Avenue",
            addressCity = "Orlando",
            addressState = "FL",
        )

        Omni.shared()?.tokenize(
            creditCard = card,
            completion = { paymentMethod ->
                log("Successfully tokenized credit card")
                log(paymentMethod.toString())
            },
            error = {
                log("Couldn't tokenize card: ${it.message}. ${it.detail}")
            }
        )
    }

    /**
     * Show reader details
     */
    fun onGetConnectedReaderDetails() {
        Omni.shared()?.getConnectedReader(
            onReaderFound = { reader ->
                if (reader != null) {
                    log("Connected Reader:")
                    log(reader.toString())
                } else {
                    log("There is no connected reader")
                }
            },
            onFail = {
                log(it.toString())
            }
        )
    }

    /**
     * Disconnect the current reader
     */
    fun onDisconnectReader() {
        Omni.shared()?.apply {
            disconnectReader(
                mobileReader = null,
                onDisconnected = {
                    log("Reader disconnected")
                    reader = null
                },
                onFail = { log(it.toString()) }
            )
        }
    }

    fun onCancelTransaction() {
        Omni.shared()?.cancelMobileReaderTransaction(
            completion = {
                log("Successfully canceled the transaction")
                Omni.shared()?.disconnectReader(
                    mobileReader = null,
                    onDisconnected = {
                        log("Reader disconnected (from cancel)")
                        reader = null
                    },
                    onFail = {
                        log(it.toString())
                    }
                )
            },
            error = {
                log(it.toString())
            }
        )
    }

    override fun onUsbAccessoryConnected() {
        log("IDTech Accessory Connected!")
    }

    override fun onUsbAccessoryDisconnected() {
        log("IDTech Accessory Disconnected!")
    }
}
