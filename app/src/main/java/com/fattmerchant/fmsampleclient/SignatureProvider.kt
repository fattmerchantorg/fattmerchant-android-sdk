package com.fattmerchant.fmsampleclient

import com.fattmerchant.omni.SignatureProviding

class SignatureProvider: SignatureProviding {

    override fun signatureRequired(completion: (String) -> Unit) {
        completion("signature")
    }

}