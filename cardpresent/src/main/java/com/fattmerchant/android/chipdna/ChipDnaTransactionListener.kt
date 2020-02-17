package com.fattmerchant.android.chipdna

import com.creditcall.chipdnamobile.*

class ChipDnaTransactionListener : ITransactionUpdateListener, ITransactionFinishedListener,
    IDeferredAuthorizationListener, ISignatureVerificationListener, IVoiceReferralListener,
    IPartialApprovalListener, IForceAcceptanceListener, IVerifyIdListener {

    var onFinish: ((Parameters) -> Unit)? = null

    override fun onTransactionFinishedListener(parameters: Parameters) {
        onFinish?.invoke(parameters)
    }

    override fun onSignatureVerification(parameters: Parameters) {
        if (parameters.getValue(ParameterKeys.ResponseRequired) != ParameterValues.TRUE) {
            return
        }

        val approveSignatureParams = Parameters()
        approveSignatureParams.add(ParameterKeys.Result, ParameterValues.TRUE)

        ChipDnaMobile.getInstance().continueSignatureVerification(approveSignatureParams)
    }

    override fun onTransactionUpdateListener(parameters: Parameters) {}
    override fun onVoiceReferral(parameters: Parameters) {}
    override fun onVerifyId(parameters: Parameters) {}
    override fun onDeferredAuthorizationListener(parameters: Parameters) {}
    override fun onForceAcceptance(parameters: Parameters) {}
    override fun onPartialApproval(parameters: Parameters) {}
}