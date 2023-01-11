package com.fattmerchant.fmsampleclient

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.creditcall.chipdnamobile.ChipDnaMobile
import com.creditcall.chipdnamobile.ChipDnaMobileSerializer
import com.creditcall.chipdnamobile.IAvailablePinPadsListener
import com.creditcall.chipdnamobile.IConnectAndConfigureFinishedListener
import com.creditcall.chipdnamobile.IDeferredAuthorizationListener
import com.creditcall.chipdnamobile.IForceAcceptanceListener
import com.creditcall.chipdnamobile.IPartialApprovalListener
import com.creditcall.chipdnamobile.IProcessReceiptFinishedListener
import com.creditcall.chipdnamobile.ISignatureVerificationListener
import com.creditcall.chipdnamobile.ITransactionFinishedListener
import com.creditcall.chipdnamobile.ITransactionUpdateListener
import com.creditcall.chipdnamobile.IVerifyIdListener
import com.creditcall.chipdnamobile.IVoiceReferralListener
import com.creditcall.chipdnamobile.ParameterKeys
import com.creditcall.chipdnamobile.ParameterValues
import com.creditcall.chipdnamobile.Parameters
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date

class ChipDnaHelper(
    var apiKey: String? = "v8EknC7d3rhgyvDWrSrU6QM2PBT573K2",
    var environment: String? = ParameterValues.LiveEnvironment,
    var appId: String? = "FMDEMO"
) : IConnectAndConfigureFinishedListener, IAvailablePinPadsListener {

    data class SelectablePinPad(var name: String, var connectionType: String)

    companion object {

        /** Authenticates with ChipDNA */
        class ChipDnaAuthTask(var context: WeakReference<Context>? = null) : AsyncTask<String, Void, Parameters>() {

            interface ChipDnaAuthTaskResponse {
                fun processFinish(parameters: Parameters?)
            }

            var delegate: WeakReference<ChipDnaAuthTaskResponse>? = null

            override fun doInBackground(vararg params: String): Parameters? {
                return context?.get()?.let { context ->
                    val parameters = Parameters()
                    parameters.add(ParameterKeys.Password, params.first())
                    return ChipDnaMobile.initialize(context, parameters)
                }
            }

            override fun onPostExecute(response: Parameters?) {
                delegate?.get()?.processFinish(response)
            }
        }

        /** Deserializes available pin pads from a [Parameters] object */
        class DeserializePinPadTask(var context: WeakReference<Context>? = null) :
            AsyncTask<String, Void, List<SelectablePinPad>>() {
            interface DeserializePinPadTaskResponse {
                fun processFinish(availableReaders: List<SelectablePinPad>?)
            }

            var delegate: WeakReference<DeserializePinPadTaskResponse>? = null

            override fun doInBackground(vararg params: String): List<SelectablePinPad> {
                val availablePinPadsList = ArrayList<SelectablePinPad>()
                try {
                    val availablePinPadsHashMap = ChipDnaMobileSerializer.deserializeAvailablePinPads(params[0])

                    for (connectionType in availablePinPadsHashMap.keys) {
                        for (pinpad in availablePinPadsHashMap[connectionType]!!) {
                            availablePinPadsList.add(SelectablePinPad(pinpad, connectionType))
                        }
                    }
                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                return availablePinPadsList
            }

            override fun onPostExecute(availablePinPadsList: List<SelectablePinPad>?) {
                delegate?.get()?.processFinish(availablePinPadsList)
            }
        }

        class ConnectPinPadTask(var context: WeakReference<Context>? = null) :
            AsyncTask<SelectablePinPad, Void, Parameters>() {
            interface ConnectPinPadTaskResponse {
                fun processFinish(parameters: Parameters)
            }

            var delegate: WeakReference<ConnectPinPadTaskResponse>? = null

            override fun doInBackground(vararg params: SelectablePinPad?): Parameters {
                val pinpad = params.first()!!
                var requestParams = Parameters()
                requestParams.add(ParameterKeys.PinPadName, pinpad.name)
                requestParams.add(ParameterKeys.PinPadConnectionType, pinpad.connectionType)
                return ChipDnaMobile.getInstance().setProperties(requestParams)
            }

            override fun onPostExecute(result: Parameters) {
                Log.d("connectpintask", result.toString())
                delegate?.get()?.processFinish(result)
            }
        }
    }

    fun submitTransaction() {
        val params = Parameters()
        params.add(ParameterKeys.Amount, "01")
        params.add(ParameterKeys.AmountType, ParameterValues.AmountTypeActual)
        params.add(ParameterKeys.Currency, "USD")

        val userRef = String.format("CDM-%s", SimpleDateFormat("yy-MM-dd-HH.mm.ss").format(Date()))
        params.add(ParameterKeys.UserReference, userRef)
        params.add(ParameterKeys.TransactionType, ParameterValues.Sale)
        params.add(ParameterKeys.PaymentMethod, ParameterValues.Card)

        doAuthorizeTransaction(params)
    }

    fun doAuthorizeTransaction(params: Parameters) {
        Log.d("doAuthorizeTran", params.toString())

        var p = Parameters()

        val response = ChipDnaMobile.getInstance().startTransaction(params)
        if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equals(ParameterValues.FALSE)) {
        }
    }

    fun searchForReaders() {
        val parameters = Parameters().apply {
            add(ParameterKeys.SearchConnectionTypeBluetooth, ParameterValues.TRUE)
        }
        ChipDnaMobile.getInstance().clearAllAvailablePinPadsListeners()
        ChipDnaMobile.getInstance().addAvailablePinPadsListener(this)
        ChipDnaMobile.getInstance().getAvailablePinPads(parameters)
    }

    fun registerListeners() {
        ChipDnaMobile.getInstance().addConnectAndConfigureFinishedListener(this)

        val transactionListener = TransactionListener()
        ChipDnaMobile.getInstance().addTransactionUpdateListener(transactionListener)
        ChipDnaMobile.getInstance().addTransactionFinishedListener(transactionListener)
        ChipDnaMobile.getInstance().addDeferredAuthorizationListener(transactionListener)
        ChipDnaMobile.getInstance().addSignatureVerificationListener(transactionListener)
        ChipDnaMobile.getInstance().addVoiceReferralListener(transactionListener)
        ChipDnaMobile.getInstance().addPartialApprovalListener(transactionListener)
        ChipDnaMobile.getInstance().addForceAcceptanceListener(transactionListener)
        ChipDnaMobile.getInstance().addVerifyIdListener(transactionListener)

        ChipDnaMobile.getInstance().addProcessReceiptFinishedListener(ProcessReceiptListener())
    }

    fun initialize(appContext: Context, completion: (() -> Unit)? = null) {
        val authTask = ChipDnaAuthTask(WeakReference(appContext))
        authTask.delegate = WeakReference(object : ChipDnaAuthTask.ChipDnaAuthTaskResponse {
            override fun processFinish(parameters: Parameters?) {
                setCredentials()
//                registerListeners()
                completion?.invoke()
//                val response =
//                    ChipDnaMobile.getInstance().connectAndConfigure(ChipDnaMobile.getInstance().getStatus(null))
//                if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equals(
//                        ParameterValues.FALSE
//                    )
//                ) {
//                    completion?.invoke()
//                }
            }
        })

        authTask.execute("password")
    }

    private fun setCredentials() {
        if (appId == null || apiKey == null || environment == null) {
            return
        }

        // Credentials are set in ChipDnaMobile Status object. It's recommended that you fetch fresh ChipDnaMobile Status object each time you wish to make changes.
        // This ensures the set of properties used is always up to date with the version of properties in ChipDnaMobile
        val statusParameters = ChipDnaMobile.getInstance().getStatus(null)

        // Credentials are returned to ChipDnaMobile as a set of Parameters
        val requestParameters = Parameters()

        requestParameters.add(ParameterKeys.ApiKey, apiKey)
        requestParameters.add(ParameterKeys.Environment, environment)
        requestParameters.add(ParameterKeys.ApplicationIdentifier, appId?.uppercase())

        // Once all changes have been made a call to .setProperties() is required in order for the changes to take effect.
        // Parameters are passed within this method and added to the ChipDna Mobile status object.
        ChipDnaMobile.getInstance().setProperties(requestParameters)
    }

    /*
    Listeners
     */

    override fun onConnectAndConfigureFinished(params: Parameters?) {
        System.out.println("Connected and configured")
    }

    fun connectForReal() {
        var response = ChipDnaMobile.getInstance().connectAndConfigure(ChipDnaMobile.getInstance().getStatus(null))
        registerListeners()
        Log.d("connectforreal", response.toString())
    }

    override fun onAvailablePinPads(parameters: Parameters?) {
        val availablePinPadsXml = parameters?.getValue(ParameterKeys.AvailablePinPads)
        val task = DeserializePinPadTask()
        task.delegate = WeakReference(object : DeserializePinPadTask.DeserializePinPadTaskResponse {
            override fun processFinish(availableReaders: List<SelectablePinPad>?) {
                availableReaders?.first()?.let {
                    val task = ConnectPinPadTask()
                    task.delegate = WeakReference(object : ConnectPinPadTask.ConnectPinPadTaskResponse {
                        override fun processFinish(parameters: Parameters) {
                            connectForReal()
                        }
                    })
                    task.execute(it)
                }
            }
        })
        task.execute(availablePinPadsXml)
    }

    private inner class ProcessReceiptListener : IProcessReceiptFinishedListener {
        override fun onProcessReceiptFinishedListener(parameters: Parameters) {
            Log.d("receipt", parameters.toString())
        }
    }

    private inner class TransactionListener :
        ITransactionUpdateListener,
        ITransactionFinishedListener,
        IDeferredAuthorizationListener,
        ISignatureVerificationListener,
        IVoiceReferralListener,
        IPartialApprovalListener,
        IForceAcceptanceListener,
        IVerifyIdListener {
        override fun onTransactionUpdateListener(parameters: Parameters) {
            Log.d("transaction", (parameters.getValue(ParameterKeys.TransactionUpdate)))
        }

        override fun onTransactionFinishedListener(parameters: Parameters) {
            Log.d("transactionFinished", parameters.toString())
        }

        override fun onSignatureVerification(parameters: Parameters) {
            Log.d("transaction", "Signature Check Required")

            if (parameters.getValue(ParameterKeys.ResponseRequired) != ParameterValues.TRUE) {
                // Signature handled on PINpad. No call to ChipDna Mobile required.
                return
            }

            val operatorPinRequired = parameters.getValue(ParameterKeys.OperatorPinRequired) == ParameterValues.TRUE
            val receiptDataXml = parameters.getValue(ParameterKeys.ReceiptData)

            val approveSignatureParams = Parameters()
            approveSignatureParams.add(ParameterKeys.Result, ParameterValues.TRUE)

            ChipDnaMobile.getInstance().continueSignatureVerification(approveSignatureParams)
//            Thread(Runnable { requestSignatureCheck(operatorPinRequired, false, receiptDataXml) }).start()
        }

        override fun onVoiceReferral(parameters: Parameters) {
            Log.d("transaction", "Voice Referral Check Required")

            if (parameters.getValue(ParameterKeys.ResponseRequired) != ParameterValues.TRUE) {
                // Voice referral handled on PINpad. No call to ChipDna Mobile required.
                return
            }

            val phoneNumber = parameters.getValue(ParameterKeys.ReferralNumber)
            val operatorPinRequired = parameters.getValue(ParameterKeys.OperatorPinRequired) == ParameterValues.TRUE

//            Thread(Runnable { requestVoiceReferral(phoneNumber, operatorPinRequired) }).start()
        }

        /*
            Other ChipDna Mobile Callbacks, not required in this demo.
            You may need to implement some of these depending on what your terminal supports.
          */

        override fun onVerifyId(parameters: Parameters) {
            Log.d("transaction", parameters.toString())
        }

        override fun onDeferredAuthorizationListener(parameters: Parameters) {
            Log.d("transaction", parameters.toString())
        }

        override fun onForceAcceptance(parameters: Parameters) {
            Log.d("transaction", parameters.toString())
        }

        override fun onPartialApproval(parameters: Parameters) {
            Log.d("transaction", parameters.toString())
        }
    }
}
