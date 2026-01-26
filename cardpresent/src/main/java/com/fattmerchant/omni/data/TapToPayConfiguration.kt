package com.fattmerchant.omni.data

/**
 * Configuration for Tap to Pay on Android functionality.
 *
 * This class manages the settings for Tap to Pay, which allows merchants to accept
 * contactless payments directly on their Android device without external hardware.
 *
 * ## Transaction Method Selection (Automatic)
 *
 * When you call `takeMobileReaderTransaction()`, the SDK automatically determines which payment
 * method to use based on this configuration and device connection status:
 *
 * - **Hybrid Mode** (`enabled=true, allowExternalReaders=true`):
 *   - If external reader connected → Uses external reader
 *   - If NO external reader → Uses NFC Tap to Pay
 *
 * - **Tap to Pay Only** (`enabled=true, allowExternalReaders=false`):
 *   - Always uses NFC Tap to Pay
 *   - External readers are disabled
 *
 * - **External Readers Only** (`enabled=false`):
 *   - Only uses external readers
 *   - NFC Tap to Pay is disabled
 *
 * The SDK automatically configures the payment processor based on device state.
 * You don't need to specify which method to use in your transaction code.
 *
 * ## Certificate Fingerprint
 *
 * By default, the SDK automatically extracts the certificate fingerprint from your app's signing
 * certificate at runtime. However, you can optionally provide your own fingerprint by setting
 * the `certificateFingerprint` parameter.
 *
 * @property enabled Whether Tap to Pay is enabled. When true, the device's NFC will be used to accept payments.
 * @property allowExternalReaders Whether to also support external Bluetooth/USB readers alongside Tap to Pay.
 *                                When true: TapToMobilePOI=TRUE and PaymentDevicePOI=TRUE (hybrid mode)
 *                                When false: TapToMobilePOI=TRUE and PaymentDevicePOI=FALSE (Tap to Pay only)
 * @property certificateFingerprint Optional SHA-256 certificate fingerprint. If null, SDK will automatically extract it.
 * @property testMode Whether to use test mode (sandbox) or production.
 *                    Test mode connects to sandbox environment and works with the Test Card Simulator app.
 * @property mockMode When true, bypasses connection checks to allow UI testing during development.
 *                    Use this to test the NFC prompt and UI flow during development.
 *                    Does NOT bypass actual transaction processing.
 *                    
 *                    **IMPORTANT: Only use mockMode for UI/UX testing during development.**
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
         * 
         * @param certificateFingerprint Optional SHA-256 certificate fingerprint. If null, SDK will automatically extract it.
         * @param testMode Whether to use test mode (sandbox) or production.
         * @param mockMode When true, bypasses connection checks for UI testing.
         */
        fun tapToPayOnly(
            certificateFingerprint: String? = null,
            testMode: Boolean = false,
            mockMode: Boolean = false
        ): TapToPayConfiguration {
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
         * 
         * @param certificateFingerprint Optional SHA-256 certificate fingerprint. If null, SDK will automatically extract it.
         * @param testMode Whether to use test mode (sandbox) or production.
         * @param mockMode When true, bypasses connection checks for UI testing.
         */
        fun hybrid(
            certificateFingerprint: String? = null,
            testMode: Boolean = false,
            mockMode: Boolean = false
        ): TapToPayConfiguration {
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
