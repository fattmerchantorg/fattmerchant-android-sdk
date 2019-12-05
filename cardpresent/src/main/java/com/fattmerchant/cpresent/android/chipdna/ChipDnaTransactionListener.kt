package com.fattmerchant.cpresent.android.chipdna

import android.util.Log
import com.creditcall.chipdnamobile.*

class ChipDnaTransactionListener : ITransactionUpdateListener, ITransactionFinishedListener,
    IDeferredAuthorizationListener, ISignatureVerificationListener, IVoiceReferralListener,
    IPartialApprovalListener, IForceAcceptanceListener, IVerifyIdListener {

    var onFinish: ((Parameters) -> Unit)? = null

    override fun onTransactionUpdateListener(parameters: Parameters) {
        Log.d("transaction", (parameters.getValue(ParameterKeys.TransactionUpdate)))
    }

    override fun onTransactionFinishedListener(parameters: Parameters) {
        Log.d("transactionFinished", parameters.toString())
        onFinish?.invoke(parameters)
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
    }

    override fun onVoiceReferral(parameters: Parameters) {
        Log.d("transaction", "Voice Referral Check Required")

        if (parameters.getValue(ParameterKeys.ResponseRequired) != ParameterValues.TRUE) {
            // Voice referral handled on PINpad. No call to ChipDna Mobile required.
            return
        }

        val phoneNumber = parameters.getValue(ParameterKeys.ReferralNumber)
        val operatorPinRequired = parameters.getValue(ParameterKeys.OperatorPinRequired) == ParameterValues.TRUE
    }

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