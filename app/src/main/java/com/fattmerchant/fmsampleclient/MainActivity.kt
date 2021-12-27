package com.fattmerchant.fmsampleclient

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.fattmerchant.android.InitParams
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.TransactionUpdateListener
import com.fattmerchant.omni.UserNotificationListener
import com.fattmerchant.omni.data.*
import com.fattmerchant.omni.data.models.*
import com.fattmerchant.omni.networking.OmniApi
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger


class MainActivity : AppCompatActivity(), PermissionsManager {

    val staxKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJtZXJjaGFudCI6ImViNDhlZjk5LWFhNzgtNDk2ZS05YjAxLTQyMWY4ZGFmNzMyMyIsImdvZFVzZXIiOnRydWUsImJyYW5kIjoiZmF0dG1lcmNoYW50Iiwic3ViIjoiMzBjNmVlYjYtNjRiNi00N2Y2LWJjZjYtNzg3YTljNTg3OThiIiwiaXNzIjoiaHR0cDovL2FwaWRldjAxLmZhdHRsYWJzLmNvbS9hdXRoZW50aWNhdGUiLCJpYXQiOjE2NDA1NzA4MDAsImV4cCI6MTY0MDY1NzIwMCwibmJmIjoxNjQwNTcwODAwLCJqdGkiOiJ3SjlDa0tqRGNlRHRzMzBhIn0.WcFvqSf0wDungNBPOX4nWfiGAv4uX8sXRVfMMCNx6LU"

    val log = Logger.getLogger("MainActivity")

    var connectedReader: MobileReader? = null

    fun log(msg: String?) {
        log.info("[${Thread.currentThread().name}] $msg")
    }

    var transaction: Transaction? = null

    fun getAmount(): Int {
        return textInput_amount.text.toString().toFloat().times(100).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButtons()
        initializeOmni(staxKey)
        textView.movementMethod = ScrollingMovementMethod()
//        showApiKeyDialog()
    }

    override var permissionRequestLauncherCallback: ((Boolean) -> Unit)? = null
    override fun getActivity(): AppCompatActivity {
        return this
    }

    override fun getContext(): Context {
        return this
    }

    override var permissionRequestLauncher
            = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        permissionRequestLauncherCallback?.invoke(isGranted)
    }

    private fun setupPerformSaleWithReaderButton() {
        buttonPerformSaleWithReader.setOnClickListener {
            val amount = Amount(getAmount())
            updateStatus("Attempting to charge ${amount.dollarsString()}")
            val request = TransactionRequest(amount)
//            request.customerId = "bbe13c96-8bf6-4cb5-8d5c-24896cf0e0db"

            // Listen to transaction updates delivered by the Omni SDK
            Omni.shared()?.transactionUpdateListener = object: TransactionUpdateListener {
                override fun onTransactionUpdate(transactionUpdate: TransactionUpdate) {
                    updateStatus("${transactionUpdate.value} | ${transactionUpdate.userFriendlyMessage}")
                }
            }

            Omni.shared()?.userNotificationListener = object: UserNotificationListener {
                override fun onUserNotification(userNotification: UserNotification) {
                    updateStatus("${userNotification.value} | ${userNotification.userFriendlyMessage}")
                }

                override fun onRawUserNotification(userNotification: String) {
                    updateStatus(userNotification)
                }
            }

            Omni.shared()?.takeMobileReaderTransaction(request, {

                val msg = if (it.success == true) {
                    "Successfully executed transaction"
                } else {
                    "Transaction declined"
                }

                runOnUiThread {
                    updateStatus(msg)
                }

                transaction = it
            }, {
                updateStatus("Couldn't perform sale: ${it.message}. ${it.detail}")
            })
        }

        buttonPerformSaleWithReader.isEnabled = true
    }

    private fun setupPerformSaleWithTerminalButton() {
        buttonPerformSaleWithTerminal.setOnClickListener {
            val amount = Amount(getAmount())
            updateStatus("Attempting to charge ${amount.dollarsString()}")
            val request = TransactionRequest(amount)

            Omni.shared()?.takePaymentTerminalTransaction(request, {
                val msg = if (it.success == true) {
                    "Successfully executed transaction"
                } else {
                    "Transaction declined"
                }

                runOnUiThread {
                    updateStatus(msg)
                }

                transaction = it
            }, {
                updateStatus("Couldn't perform sale: ${it.message}. ${it.detail}")
            })
        }

        buttonPerformSaleWithReader.isEnabled = true
    }

    private fun setupPerformSaleButton() {
        buttonPerformSale.setOnClickListener {
            val amount = Amount(getAmount())
            updateStatus("Attempting to charge ${amount.dollarsString()}")
            val request = TransactionRequest(amount, CreditCard("Test Payment", "4111111111111111", "0224", "32812"))

            Omni.shared()?.pay(request, {
                val msg = if (it.success == true) {
                    "Successfully executed transaction"
                } else {
                    "Transaction declined"
                }

                runOnUiThread {
                    updateStatus(msg)
                }

                transaction = it
            }, {
                updateStatus("Couldn't perform sale: ${it.message}. ${it.detail}")
            })
        }

        buttonPerformSale.isEnabled = true
    }

    private fun setupPerformAuthButton() {
        buttonPerformAuth.setOnClickListener {
            val amount = Amount(getAmount())
            updateStatus("Attempting to auth ${amount.dollarsString()}")
            val request = TransactionRequest(amount)
            request.preauth = true

            Omni.shared()?.takeMobileReaderTransaction(request, {

                val msg = if (it.success == true) {
                    "Successfully authed transaction"
                } else {
                    "Transaction declined"
                }

                runOnUiThread {
                    updateStatus(msg)
                }

                transaction = it
            }, {
                updateStatus("Couldn't perform auth: ${it.message}. ${it.detail}")
            })
        }
    }

    private fun setupCaptureLastAuthButton() {
        buttonCaptureLastAuth.setOnClickListener {
            if (transaction?.id == null) { return@setOnClickListener }
            val transactionId = transaction?.id!!

            val amount = Amount(getAmount())
            updateStatus("Attempting to capture last auth")

            Omni.shared()?.capturePreauthTransaction(transactionId, amount, {
                val msg = if (it.success == true) {
                    "Successfully captured transaction"
                } else {
                    "Transaction declined"
                }

                runOnUiThread {
                    updateStatus(msg)
                }
            }, {
                updateStatus("Couldn't perform capture: ${it.message}. ${it.detail}")
            })
        }
    }

    private fun setupVoidLastAuthButton() {
        buttonVoidLastAuth.setOnClickListener {
            if (transaction?.id == null) { return@setOnClickListener }
            val transactionId = transaction?.id!!

            Omni.shared()?.voidTransaction(transactionId, {
                val msg = if (it.success == true) {
                    "Successfully voided transaction"
                } else {
                    "Transaction declined"
                }

                runOnUiThread {
                    updateStatus(msg)
                }
            }, {
                updateStatus("Couldn't perform void: ${it.message}. ${it.detail}")
            })
        }
    }


    private fun setupTokenizeCardButton() {
        buttonTokenizeCard.setOnClickListener {
            Omni.shared()?.tokenize(CreditCard.testCreditCard(), {
                val msg = "Successfully tokenized credit card"
                runOnUiThread {
                    updateStatus(msg)
                    updateStatus(it)
                }
            }, {
                runOnUiThread {
                    updateStatus("Couldn't tokenize card: ${it.message}. ${it.detail}")
                }
            })
        }
    }

    private fun setupTokenizeBankButton() {
        buttonTokenizeBank.setOnClickListener {
            var andre3000 = BankAccount.testBankAccount().apply {
                personName = "Andree Threestacks"
            }
            Omni.shared()?.tokenize(andre3000, {
                val msg = "Successfully tokenized bank account"
                runOnUiThread {
                    updateStatus(msg)
                    updateStatus(it)
                }
            }, {
                runOnUiThread {
                    updateStatus("Couldn't tokenize card: ${it.message}. ${it.detail}")
                }
            })
        }
    }

    private fun setupRefundButton() {
        buttonRefundPreviousTransaction.setOnClickListener {
            updateStatus("Fetching list of transactions")
            Omni.shared()?.getTransactions({ transactions ->

                // Figure out which transactions are refundable
                val refundableTransactions = transactions.filter {
                    it.source?.contains("CPSDK") == true
                            || it.source?.contains("terminalservice.dejavoo") == true
                }

                chooseTransaction(refundableTransactions) { transactionToRefund ->
                    updateStatus("Trying to refund ${transactionToRefund.pretty()}")
                    Omni.shared()?.refundMobileReaderTransaction(transactionToRefund, {
                        updateStatus("Refunded ${transactionToRefund.pretty()}")
                    }, {
                        updateStatus("Error refunding: ${it.message} ${it.detail}")
                    })
                }
            }, {
                updateStatus(it.message ?: "Could not get transactions")
            })
        }
    }

    private fun setupVoidButton() {
        buttonVoidTransaction.setOnClickListener {
            updateStatus("Fetching list of transactions")
            Omni.shared()?.getTransactions({ transactions ->

                // Figure out which transactions are refundable
                val voidableTransactions = transactions.filter {
                    it.source?.contains("CPSDK") == true
                            || it.source?.contains("terminalservice.dejavoo") == true
                }

                chooseTransaction(voidableTransactions) { transactionToRefund ->
                    updateStatus("Trying to void ${transactionToRefund.pretty()}")
                    Omni.shared()?.voidMobileReaderTransaction(transactionToRefund, {
                        updateStatus("Voided ${transactionToRefund.pretty()}")
                    }, {
                        updateStatus("Error voiding: ${it.message} ${it.detail}")
                    })
                }
            }, {
                updateStatus(it.message ?: "Could not get transactions")
            })
        }
    }

    private fun setupInitializeButton() {
        buttonInitialize.setOnClickListener {
            showApiKeyDialog()
        }
    }

    private fun setupConnectReaderButton() {
        buttonConnectReader.setOnClickListener {
            searchAndConnectReader()
        }
    }

    private fun setupReaderDetailsButton() {
        buttonConnectedReaderDetails.setOnClickListener {
            Omni.shared()?.getConnectedReader({ connectedReader ->
                connectedReader?.let { reader ->
                    updateStatus("Connected Reader:")
                    updateStatus(reader)
                } ?: updateStatus("There is no connected reader")
            }, { exception ->
                updateStatus(exception)
            }) ?: updateStatus("Could not get connected reader")
        }
    }

    private fun setupDisconnectReaderButton() {
        buttonDisconnectReader.setOnClickListener {
            Omni.shared()?.getConnectedReader({ connectedReader ->
                connectedReader?.let { reader ->
                    Omni.shared()?.disconnectReader(reader, {
                        updateStatus("Reader disconnected")
                    }, {
                        updateStatus(it)
                    })
                } ?: updateStatus("There is no connected reader")
            }, { exception ->
                updateStatus(exception)
            }) ?: updateStatus("Could not get connected reader")
        }
    }

    private fun showApiKeyDialog() {
        val editText = EditText(this).apply { maxLines = 1 }
        updateStatus("Attempting to initialize CPSDK")
        AlertDialog.Builder(this)
            .setTitle("Please provide a Stax api token")
            .setView(editText)
            .setCancelable(false)
            .setPositiveButton("Done") { dialog, _ ->
                dialog.dismiss()
                // If you want to not use the apikey dialog, modify the initializeOmni call like below
                // initializeOmni("insert api key here")
                initializeOmni("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJtZXJjaGFudCI6ImViNDhlZjk5LWFhNzgtNDk2ZS05YjAxLTQyMWY4ZGFmNzMyMyIsImdvZFVzZXIiOnRydWUsImJyYW5kIjoiZmF0dG1lcmNoYW50Iiwic3ViIjoiMzBjNmVlYjYtNjRiNi00N2Y2LWJjZjYtNzg3YTljNTg3OThiIiwiaXNzIjoiaHR0cDovL2FwaWRldjAxLmZhdHRsYWJzLmNvbS9hdXRoZW50aWNhdGUiLCJpYXQiOjE2MjIxMjgyMzQsImV4cCI6MTYyMjIxNDYzNCwibmJmIjoxNjIyMTI4MjM0LCJqdGkiOiJUYU9RSnV0cElEeWx6MzNoIn0.FosF0OCb4wm3O3Uj98V23xiJ8PN9HDNAqx-k8nhlptA")
            }.show()
    }

    private fun setupButtons() {
        setupInitializeButton()
        setupPerformSaleWithReaderButton()
        setupPerformSaleWithTerminalButton()
        setupPerformSaleButton()
        setupRefundButton()
        setupConnectReaderButton()
        setupDisconnectReaderButton()
        setupVoidButton()
        setupReaderDetailsButton()
        setupDisconnectReaderButton()
        setupTokenizeBankButton()
        setupTokenizeCardButton()
        setupPerformAuthButton()
        setupCaptureLastAuthButton()
        setupVoidLastAuthButton()
    }

    private fun Transaction.pretty(): String {
        return "total: $${this.total}\nid: ${id!!.substring(0, 7)}..."
    }

    private fun chooseTransaction(transactions: List<Transaction>, completion: (Transaction) -> Unit) {
        updateStatus("Displaying list of transactions")
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Select a transaction")
                .setItems(
                    transactions.map { it.pretty() }.toTypedArray()
                ) { _, which ->
                    updateStatus("Transaction chosen: ${transactions[which].pretty()}")
                    completion(transactions[which])
                }
                .setNeutralButton("Nevermind") { dialog, _ ->
                    updateStatus("Transaction not chosen")
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()
        }
    }

    private fun searchAndConnectReader() {
        runIfPermissionGranted(
                Manifest.permission.ACCESS_FINE_LOCATION,
                R.string.permission_rationale_title,
                R.string.permission_rationale_message_fine_location,
                R.string.permission_denied_title,
                R.string.permission_rationale_message_fine_location) {
            updateStatus("Searching for readers...")
            Omni.shared()?.getAvailableReaders {
                val readers = it.map { it.getName() }.toTypedArray()
                updateStatus("Found readers: ${it.map { it.getName() }}")

                runOnUiThread {
                    AlertDialog.Builder(this@MainActivity)
                            .setItems(readers) { dialog, which ->
                                val selected = it[which]

                                updateStatus("Trying to connect to [${selected.getName()}]")

                                Omni.shared()?.connectReader(selected, { reader ->
                                    this.connectedReader = reader
                                    buttonDisconnectReader.isEnabled = true
                                    updateStatus("Connected to [${reader.getName()}]")

                                    runOnUiThread {
                                        buttonPerformSaleWithReader.isEnabled = true
                                    }
                                }, { error ->
                                    updateStatus("Error connecting: $error")
                                })

                            }.create().show()
                }
            }
        }
    }

    private fun updateStatus(reader: MobileReader) = runOnUiThread {
        val readerString = """Mobile Reader:
            Name:       ${reader.getName()}
            Serial:     ${reader.serialNumber()}
            Make:       ${reader.getMake()}
            Model:      ${reader.getModel()}
            Firmware:   ${reader.getFirmwareVersion()}
        """.trimIndent()

        updateStatus(readerString)
    }

    private fun updateStatus(msg: String) = runOnUiThread {
        val newText = formatMessage(msg) + "\n" + textView.text
        textView.text = newText
    }

    private fun updateStatus(paymentMethod: PaymentMethod) = runOnUiThread {
        var message = "PaymentMethod: "
        message += "\n\t id: ${paymentMethod.id ?: ""}"
        message += "\n\t customerId: ${paymentMethod.customerId}"
        message += "\n\t method: ${paymentMethod.method ?: ""}"
        updateStatus(message)
    }

    private fun updateStatus(exception: OmniException) = updateStatus("[${exception.message}] ${exception.detail}")

    private fun formatMessage(msg: String): String {
        val dateFormat = SimpleDateFormat("h:m:ss", Locale.US)
        return "${dateFormat.format(Date())} | $msg"
    }

    private fun initializeOmni(apiKey: String) {
        updateStatus("Trying to initialize")
        Omni.initialize(mapOf(
            "appContext" to applicationContext,
            "environment" to OmniApi.Environment.DEV,
            "authenticationKey" to "",
            "tpn" to "",
            "registerId" to "",
            "apiKey" to apiKey,
            "appName" to "stax_sdk_sample"
        ), {
                runOnUiThread {
                    updateStatus("Initialized")
                    buttonRefundPreviousTransaction.isEnabled = true
                    buttonInitialize.visibility = View.GONE
                }
                Omni.shared()?.signatureProvider = SignatureProvider()
            }
        ) {
            updateStatus("${it.message}. ${it.detail}")
        }

//        Omni.initialize(
//            InitParams(applicationContext, application, apiKey, OmniApi.Environment.DEV), {
//                runOnUiThread {
//                    updateStatus("Initialized")
//                    buttonRefundPreviousTransaction.isEnabled = true
//                    buttonInitialize.visibility = View.GONE
//                }
//                Omni.shared()?.signatureProvider = SignatureProvider()
//            }
//        ) {
//            updateStatus("${it.message}. ${it.detail}")
//        }
    }

}
