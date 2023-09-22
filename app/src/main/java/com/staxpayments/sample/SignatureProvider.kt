package com.staxpayments.sample
import com.fattmerchant.omni.SignatureProviding

class SignatureProvider : SignatureProviding {
    override fun signatureRequired(completion: (String) -> Unit) {
        completion("signature")
    }
}
