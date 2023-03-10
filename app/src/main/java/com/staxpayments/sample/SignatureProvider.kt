package com.staxpayments.sample

import com.staxpayments.sdk.SignatureProviding

class SignatureProvider : SignatureProviding {
    override fun signatureRequired(completion: (String) -> Unit) {
        completion("signature")
    }
}
