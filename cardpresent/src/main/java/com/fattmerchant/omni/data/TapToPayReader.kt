package com.fattmerchant.omni.data

import com.fattmerchant.android.chipdna.ConnectionType

/**
 * Represents a virtual Tap to Pay reader for NFC-based contactless payments.
 *
 * This is not a physical device, but rather represents the device's built-in NFC
 * capability when Tap to Pay is enabled. It allows the SDK to treat Tap to Pay
 * the same way as external readers in terms of the transaction flow.
 */
class TapToPayReader : MobileReader {
    
    override fun getName(): String = "Tap to Pay"
    
    override fun getFirmwareVersion(): String? = null
    
    override fun getMake(): String? = "NMI"
    
    override fun getModel(): String? = "Contactless"
    
    override fun serialNumber(): String? = null
    
    override fun getConnectionType(): ConnectionType = ConnectionType.NFC
    
    override fun toString(): String = "TapToPayReader(name=${getName()}, make=${getMake()})"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TapToPayReader) return false
        return true // All TapToPayReader instances are considered equal
    }
    
    override fun hashCode(): Int {
        return getName().hashCode()
    }
}
