package com.staxpayments.android.chipdna

import com.creditcall.chipdnamobile.ChipDnaMobile
import com.creditcall.chipdnamobile.IApplicationSelectionListener
import com.creditcall.chipdnamobile.IDeferredAuthorizationListener
import com.creditcall.chipdnamobile.IForceAcceptanceListener
import com.creditcall.chipdnamobile.IPartialApprovalListener
import com.creditcall.chipdnamobile.ISignatureVerificationListener
import com.creditcall.chipdnamobile.ITransactionFinishedListener
import com.creditcall.chipdnamobile.ITransactionUpdateListener
import com.creditcall.chipdnamobile.IUserNotificationListener
import com.creditcall.chipdnamobile.IVerifyIdListener
import com.creditcall.chipdnamobile.IVoiceReferralListener
import com.creditcall.chipdnamobile.ParameterKeys
import com.creditcall.chipdnamobile.ParameterValues
import com.creditcall.chipdnamobile.Parameters
import com.staxpayments.sdk.SignatureProviding
import com.staxpayments.sdk.TransactionUpdateListener
import com.staxpayments.sdk.UserNotificationListener
import com.staxpayments.sdk.data.TransactionUpdate

internal class ChipDnaTransactionListener :
    ITransactionUpdateListener,
    ITransactionFinishedListener,
    IDeferredAuthorizationListener,
    ISignatureVerificationListener,
    IVoiceReferralListener,
    IPartialApprovalListener,
    IForceAcceptanceListener,
    IVerifyIdListener,
    IApplicationSelectionListener,
    IUserNotificationListener {

    /** Called when the Transaction is complete */
    var onFinish: ((Parameters) -> Unit)? = null

    /** Provides a signature */
    var signatureProvider: SignatureProviding? = null

    /** Gets notified of transaction events */
    var transactionUpdateListener: TransactionUpdateListener? = null

    /** Gets notified of user notifications */
    var userNotificationListener: UserNotificationListener? = null

    override fun onTransactionFinishedListener(parameters: Parameters) {
        onFinish?.invoke(parameters)
    }

    override fun onSignatureVerification(parameters: Parameters) {
        val signatureProvider = signatureProvider

        // If we have a signature provider, ask it for the signature
        // Else make an empty signature
        if (signatureProvider != null) {
            transactionUpdateListener?.onTransactionUpdate(TransactionUpdate.PromptProvideSignature)
            signatureProvider.signatureRequired { signature ->
                // ChipDna says that SignatureData is a string-encoded image. The burden of
                // encoding the image is on the integrating application. By the time we hit this
                // block, the encoding is already done and the all we get is the string which we
                // can pass through to ChipDna
                transactionUpdateListener?.onTransactionUpdate(TransactionUpdate.SignatureProvided)
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

    override fun onTransactionUpdateListener(parameters: Parameters) {
        mapTransactionUpdate(parameters.getValue(ParameterKeys.TransactionUpdate))?.let {
            transactionUpdateListener?.onTransactionUpdate(it)
        }
    }

    override fun onUserNotification(parameters: Parameters?) {
        parameters?.getValue(ParameterKeys.UserNotification)?.let {
            userNotificationListener?.onRawUserNotification(it)
            mapUserNotification(it)?.let { userNotification ->
                userNotificationListener?.onUserNotification(userNotification)
            }
        }
    }

    override fun onVoiceReferral(parameters: Parameters) {}
    override fun onVerifyId(parameters: Parameters) {}
    override fun onDeferredAuthorizationListener(parameters: Parameters) {}
    override fun onForceAcceptance(parameters: Parameters) {}
    override fun onPartialApproval(parameters: Parameters) {}
    override fun onApplicationSelection(p0: Parameters?) {}
}
