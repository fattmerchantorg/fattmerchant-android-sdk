package com.fattmerchant.cpresent.omni.entity.repository

import com.fattmerchant.cpresent.omni.entity.MobileReaderDriver
import com.fattmerchant.cpresent.omni.entity.models.Transaction

interface MobileReaderDriverRepository {
    suspend fun getDrivers(): List<MobileReaderDriver>

    /**
     * Returns the driver that knows how to handle the given transaction
     *
     * @return MobileReaderDriver - The driver for this kind of transaction or null if not found
     * */
    suspend fun getDriverFor(transaction: Transaction): MobileReaderDriver?
}