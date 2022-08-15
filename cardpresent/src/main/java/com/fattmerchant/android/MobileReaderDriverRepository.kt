package com.fattmerchant.android

import com.fattmerchant.android.anywherecommerce.AWCDriver
import com.fattmerchant.android.chipdna.ChipDnaDriver
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.MobileReaderDriver
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository

internal class MobileReaderDriverRepository : MobileReaderDriverRepository {

    private var chipDna = ChipDnaDriver()
    private var awc = AWCDriver()

    override suspend fun getDrivers(): List<MobileReaderDriver> {
        return listOf(awc, chipDna)
    }

    override suspend fun getInitializedDrivers(): List<MobileReaderDriver> {
        return getDrivers().filter { it.isInitialized() }
    }

    override suspend fun getDriverFor(transaction: Transaction): MobileReaderDriver? {
        if (transaction.source?.contains("CPSDK") != true) {
            return null
        }

        getInitializedDrivers().forEach {
            if (transaction.source?.contains(it.source) == true) {
                return it
            }
        }

        return null
    }

    override suspend fun getDriverFor(mobileReader: MobileReader): MobileReaderDriver? {
        mobileReader.serialNumber()?.let { serial ->
            return getInitializedDrivers().firstOrNull {
                it.familiarSerialNumbers.contains(serial)
            }
        } ?: return null
    }
}
