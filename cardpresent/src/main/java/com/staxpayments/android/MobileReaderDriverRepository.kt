package com.staxpayments.android

import com.staxpayments.android.chipdna.ChipDnaDriver
import com.staxpayments.sdk.data.MobileReader
import com.staxpayments.sdk.data.MobileReaderDriver
import com.staxpayments.sdk.data.models.Transaction
import com.staxpayments.sdk.data.repository.MobileReaderDriverRepository

internal class MobileReaderDriverRepository : MobileReaderDriverRepository {

    private var chipDna = ChipDnaDriver()

    override suspend fun getDrivers(): List<MobileReaderDriver> {
        return listOf(chipDna)
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
