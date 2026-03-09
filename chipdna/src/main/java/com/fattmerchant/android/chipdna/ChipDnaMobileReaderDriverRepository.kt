package com.fattmerchant.android.chipdna

import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.MobileReaderDriver
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository

/**
 * [MobileReaderDriverRepository] implementation backed by the ChipDNA (NMI) driver.
 *
 * Consumer apps should create an instance of this class and pass it via
 * [com.fattmerchant.android.InitParams.mobileReaderDriverRepository] to enable
 * card-present payment processing.
 */
class ChipDnaMobileReaderDriverRepository : MobileReaderDriverRepository {

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

    override suspend fun getDriverFor(mobileReader: MobileReader?): MobileReaderDriver? {
        return getInitializedDrivers().firstOrNull()
    }
}
