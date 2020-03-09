package com.fattmerchant.omni

/** Provides a signature to Omni when required */
interface SignatureProviding {

    /**
     * Called when a transaction requires a signature
     *
     * @param completion a block to run once the signature is complete. This should be given the signature
     */
    fun signatureRequired(completion: (String) -> Unit)

}
