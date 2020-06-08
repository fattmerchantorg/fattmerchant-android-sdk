package com.fattmerchant.android.anywherecommerce

import com.anywherecommerce.android.sdk.devices.CardReader
import com.fattmerchant.omni.data.MobileReader

internal fun CardReader.toMobileReader(): MobileReader {
    return object: MobileReader {
        override fun getName(): String = serialNumber
        override fun getFirmwareVersion(): String? = firmwareVersion
        override fun getMake(): String? = null
        override fun getModel(): String? = modelDisplayName
        override fun serialNumber(): String? = serialNumber
    }
}