package com.fattmerchant.fmsampleclient

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import com.fattmerchant.android.InitParams
import com.fattmerchant.android.Omni
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.networking.OmniApi
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {

    val log = Logger.getLogger("MainActivity")
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

    private fun setupPerformSaleButton() {
        buttonPerformSale.setOnClickListener {
            val amount = Amount(getAmount())
            updateStatus("Attempting to charge ${amount.dollarsString()}")
            val request = TransactionRequest(amount)
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

        buttonPerformSale.isEnabled = true
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
        buttonVoidPreviousTransaction.setOnClickListener {
            updateStatus("Fetching list of transactions")
            Omni.shared()?.getTransactions({ transactions ->

                // Figure out which transactions are refundable
                val voidableTransactions = transactions.filter {
                    it.source?.contains("CPSDK") == true && it.isVoided == false
                }

                chooseTransaction(voidableTransactions) { transactionToRefund ->
                    updateStatus("Trying to void ${transactionToRefund.pretty()}")
                    Omni.shared()?.voidMobileReaderTransaction(transactionToRefund, {
                        updateStatus("Voided ${transactionToRefund.pretty()}")
                    }, {
                        updateStatus("Error Voiding: ${it.message} ${it.detail}")
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

        buttonConnectReader.isEnabled = false
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
                initializeOmni(editText.text.toString())
            }.show()
    }

    private fun setupButtons() {
        setupInitializeButton()
        setupPerformSaleButton()
        setupRefundButton()
        setupConnectReaderButton()
        setupVoidButton()
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
                            updateStatus("Connected to [${reader.getName()}]")

                            runOnUiThread {
                                buttonPerformSale.isEnabled = true
                                buttonConnectReader.visibility = View.GONE
                            }
                        }, { error ->
                            updateStatus("Error connecting: $error")
                        })

                    }.create().show()
            }
        }
    }

    private fun updateStatus(msg: String) = runOnUiThread {
        val newText = formatMessage(msg) + "\n" + textView.text
        textView.text = newText
    }

    private fun formatMessage(msg: String): String {
        val dateFormat = SimpleDateFormat("h:m:ss", Locale.US)
        return "[${dateFormat.format(Date())}] $msg"
    }

    private fun initializeOmni(apiKey: String) {
        Omni.initialize(
            InitParams(applicationContext, apiKey, OmniApi.Environment.DEV), {
                runOnUiThread {
                    updateStatus("Initialized")
                    buttonConnectReader.isEnabled = true
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
