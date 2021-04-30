package com.fattmerchant.fmsampleclient

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
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
        showApiKeyDialog()
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

            request.invoiceId = ""

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
        buttonCancelTransaction.setOnClickListener {
            Omni.shared()?.cancelMobileReaderTransaction({
                if (it) {
                    updateStatus("Transaction cancelled")
                } else {
                    updateStatus("Transaction not cancelled")
                }

            }, {
                updateStatus(it)
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
            .setTitle("Please provide an Omni api token")
            .setView(editText)
            .setCancelable(false)
            .setPositiveButton("Done") { dialog, _ ->
                dialog.dismiss()
                // If you want to not use the apikey dialog, modify the initializeOmni call like below
                // initializeOmni("insert api key here")
                initializeOmni("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJtZXJjaGFudCI6ImU3MTJhZThlLTIwOWUtNGNkYi05MDMwLTc1NWU2OWFmMTI0NiIsImdvZFVzZXIiOnRydWUsImJyYW5kIjoiZmF0dG1lcmNoYW50Iiwic3ViIjoiMTI4MGRkNGQtMTI2MS00YWI1LThkZmItY2FmMWY3ZDhjZTM0IiwiaXNzIjoiaHR0cDovL2FwaWRldjAxLmZhdHRsYWJzLmNvbS9hdXRoZW50aWNhdGUiLCJpYXQiOjE2MTk4MDYzOTUsImV4cCI6MTYxOTg5Mjc5NSwibmJmIjoxNjE5ODA2Mzk1LCJqdGkiOiJxdllTNERKamxMUmhkSHIxIn0.V5J7StK9qpO76It94milWpXZIxbHBBmUyHjwLZGFw4w")
            }.show()
    }

    private fun setupButtons() {
        setupInitializeButton()
        setupPerformSaleWithReaderButton()
        setupPerformSaleButton()
        setupRefundButton()
        setupConnectReaderButton()
        setupDisconnectReaderButton()
        setupVoidButton()
        setupReaderDetailsButton()
        setupDisconnectReaderButton()
        setupTokenizeBankButton()
        setupTokenizeCardButton()
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
        return "[${dateFormat.format(Date())}] $msg"
    }

    private fun initializeOmni(apiKey: String) {
        Omni.initialize(
            InitParams(applicationContext, application, apiKey, OmniApi.Environment.DEV), {
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
    }

}
