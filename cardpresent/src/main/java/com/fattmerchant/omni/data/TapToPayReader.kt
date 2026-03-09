package com.fattmerchant.omni.data

/**
 * Represents a virtual Tap to Pay reader for NFC-based contactless payments.
 *
 * This is not a physical device, but rather represents the device's built-in NFC
 * capability when Tap to Pay is enabled. It allows the SDK to treat Tap to Pay
 * the same way as external readers in terms of the transaction flow.
 *
 * @property testMode Whether to use test environment. Defaults to false (production).
 */
class TapToPayReader(
    val testMode: Boolean = false
) : MobileReader {
    
    override fun getName(): String = "Tap"
    
    override fun getFirmwareVersion(): String? = null
    
    override fun getMake(): String? = "NMI"
    
    override fun getModel(): String? = "Contactless"
    
    override fun serialNumber(): String? = null
    
    override fun getConnectionType(): ConnectionType = ConnectionType.NFC
    
    override fun toString(): String = "TapToPayReader(name=${getName()}, make=${getMake()}, testMode=$testMode)"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TapToPayReader) return false
        return testMode == other.testMode
    }
    
    override fun hashCode(): Int {
        return getName().hashCode() + testMode.hashCode()
    }
}

