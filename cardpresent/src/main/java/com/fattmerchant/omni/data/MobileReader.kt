package com.fattmerchant.omni.data

/**
 * A mobile reader that can take a payment
 */
interface MobileReader {
    /** The name of the mobile reader. For example, "Miura 186" */
    fun getName(): String

    /** The firmware version of the mobile reader */
    fun getFirmwareVersion(): String?

    /** The make of the mobile reader. For example, "Miura" */
    fun getMake(): String?

    /** The model of the mobile reader. For example, "M010" */
    fun getModel(): String?

    /** The serial number of the mobile reader */
    fun serialNumber(): String?
}
