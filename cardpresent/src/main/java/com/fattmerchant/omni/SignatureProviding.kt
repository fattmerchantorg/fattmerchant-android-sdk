package com.fattmerchant.omni

/**
 * Provides a signature to Omni when required
 *
 * This should provide the signature image as a base64 encoded string
 */
interface SignatureProviding {

    /**
     * Called when a transaction requires a signature
     *
     * @param completion a block to run once the signature is complete. This should be given the
     * signature as a Base64 encoded string
     */
    fun signatureRequired(completion: (String) -> Unit)
}
