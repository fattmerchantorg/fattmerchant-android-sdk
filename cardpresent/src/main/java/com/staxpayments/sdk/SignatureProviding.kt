package com.staxpayments.sdk

/**
 * Provides a signature to Stax when required
 *
 * This should provide the signature image as a base64 encoded string
 */
interface SignatureProviding {

    /**
     * Called when a transaction requires a signature
     *
     * @param onCompletion a block to run once the signature is complete. This should be given the
     * signature as a Base64 encoded string
     */
    fun signatureRequired(onCompletion: (String) -> Unit)
}
