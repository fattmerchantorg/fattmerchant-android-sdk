package com.fattmerchant.android.chipdna

import com.creditcall.chipdnamobile.*
import com.fattmerchant.omni.SignatureProviding

class ChipDnaTransactionListener : ITransactionUpdateListener, ITransactionFinishedListener,
    IDeferredAuthorizationListener, ISignatureVerificationListener, IVoiceReferralListener,
    IPartialApprovalListener, IForceAcceptanceListener, IVerifyIdListener {

    /** Called when the Transaction is complete */
    var onFinish: ((Parameters) -> Unit)? = null

    /** Provides a signature */
    var signatureProvider: SignatureProviding? = null

    override fun onTransactionFinishedListener(parameters: Parameters) {
        onFinish?.invoke(parameters)
    }

    override fun onSignatureVerification(parameters: Parameters) {
        val signatureProvider = signatureProvider

        // If we have a signature provider, ask it for the signature
        // Else make an empty signature
        if (signatureProvider != null) {
            signatureProvider.signatureRequired { signature ->

                // ChipDna says that SignatureData is a string-encoded image. The burden of
                // encoding the image is on the integrating application. By the time we hit this
                // block, the encoding is already done and the all we get is the string which we
                // can pass through to ChipDna
                val approveSignatureParams = Parameters()
                approveSignatureParams.add(ParameterKeys.Result, ParameterValues.TRUE)
                approveSignatureParams.add(ParameterKeys.SignatureData, signature)
                ChipDnaMobile.getInstance().continueSignatureVerification(approveSignatureParams)
            }
        } else {
            if (parameters.getValue(ParameterKeys.ResponseRequired) != ParameterValues.TRUE) {
                return
            }

            val approveSignatureParams = Parameters()
            approveSignatureParams.add(ParameterKeys.Result, ParameterValues.TRUE)

            ChipDnaMobile.getInstance().continueSignatureVerification(approveSignatureParams)
        }
    }

    override fun onTransactionUpdateListener(parameters: Parameters) {}
    override fun onVoiceReferral(parameters: Parameters) {}
    override fun onVerifyId(parameters: Parameters) {}
    override fun onDeferredAuthorizationListener(parameters: Parameters) {}
    override fun onForceAcceptance(parameters: Parameters) {}
    override fun onPartialApproval(parameters: Parameters) {}
}