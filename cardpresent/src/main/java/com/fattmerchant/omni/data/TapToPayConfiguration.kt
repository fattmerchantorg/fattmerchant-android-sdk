package com.fattmerchant.omni.data

/**
 * Configuration for Tap to Pay on Android functionality.
 *
 * This class manages the settings for NMI's Tap to Pay feature, which allows merchants to accept
 * contactless payments directly on their Android device without external hardware.
 *
 * @property enabled Whether Tap to Pay is enabled. When true, the device's NFC will be used to accept payments.
 * @property allowExternalReaders Whether to also support external Bluetooth/USB readers alongside Tap to Pay.
 *                                When true: TapToMobilePOI=TRUE and PaymentDevicePOI=TRUE (hybrid mode)
 *                                When false: TapToMobilePOI=TRUE and PaymentDevicePOI=FALSE (Tap to Pay only)
 * @property certificateFingerprint The SHA-256 certificate fingerprint (Base64 encoded) for app attestation.
 *                                  Required for production use with Google Play Integrity API.
 * @property testMode Whether to use test mode (cloud-commerce-sdk-mtf) or production (cloud-commerce-sdk).
 *                    Test mode connects to NMI sandbox and works with the Test Card Simulator app.
 * @property mockMode When true, bypasses terminal.isEnabled check and simulates successful connection.
 *                    Use this to test the NFC prompt and UI flow with the NMI Test Card Simulator app
 *                    while waiting for NMI account onboarding to be completed. Does NOT bypass actual
 *                    NFC transaction processing - transactions will still fail without proper onboarding.
 *                    
 *                    **IMPORTANT: Only use mockMode for UI/UX testing. For real transactions, you MUST:**
 *                    1. Complete NMI onboarding (Step 10 in NMI documentation)
 *                    2. Email taptopay-app-onboarding@nmi.com with:
 *                       - App Package Name: com.staxpayments.sample
 *                       - Certificate Hash (SHA-256, Base64): dqWyBOJVho+5gumYDGPL1tYfQqiLVz++7gIgJRob/r0=
 *                       - Play Integrity API keys from Google Play Console
 *                    3. Wait for NMI to provision your account for Tap to Pay
 */
data class TapToPayConfiguration(
    val enabled: Boolean = false,
    val allowExternalReaders: Boolean = true,
    val certificateFingerprint: String? = null,
    val testMode: Boolean = false,
    val mockMode: Boolean = false
) {
    /**
     * Returns the ChipDNA parameter value for TapToMobilePOI based on configuration.
     */
    fun getTapToMobilePOIValue(): String {
        return if (enabled) ParameterValues.TRUE else ParameterValues.FALSE
    }

    /**
     * Returns the ChipDNA parameter value for PaymentDevicePOI based on configuration.
     */
    fun getPaymentDevicePOIValue(): String {
        return if (allowExternalReaders) ParameterValues.TRUE else ParameterValues.FALSE
    }

    companion object {
        /**
         * Creates a Tap to Pay only configuration (no external readers).
         */
        fun tapToPayOnly(testMode: Boolean = false, certificateFingerprint: String? = null, mockMode: Boolean = false): TapToPayConfiguration {
            return TapToPayConfiguration(
                enabled = true,
                allowExternalReaders = false,
                certificateFingerprint = certificateFingerprint,
                testMode = testMode,
                mockMode = mockMode
            )
        }

        /**
         * Creates a hybrid configuration (Tap to Pay + external readers).
         */
        fun hybrid(testMode: Boolean = false, certificateFingerprint: String? = null, mockMode: Boolean = false): TapToPayConfiguration {
            return TapToPayConfiguration(
                enabled = true,
                allowExternalReaders = true,
                certificateFingerprint = certificateFingerprint,
                testMode = testMode,
                mockMode = mockMode
            )
        }

        /**
         * Creates a configuration with Tap to Pay disabled (external readers only).
         */
        fun externalReadersOnly(): TapToPayConfiguration {
            return TapToPayConfiguration(
                enabled = false,
                allowExternalReaders = true,
                certificateFingerprint = null,
                testMode = false
            )
        }
    }

    // Nested object to access ParameterValues without importing ChipDNA everywhere
    private object ParameterValues {
        const val TRUE = "TRUE"
        const val FALSE = "FALSE"
    }
}
