package com.fattmerchant.cpresent.android

import com.fattmerchant.cpresent.android.chipdna.ChipDnaDriver
import com.fattmerchant.cpresent.omni.entity.MobileReader
import com.fattmerchant.cpresent.omni.entity.MobileReaderDriver
import com.fattmerchant.cpresent.omni.entity.TransactionRequest
import com.fattmerchant.cpresent.omni.entity.TransactionResult
import com.fattmerchant.cpresent.omni.entity.models.Transaction
import com.fattmerchant.cpresent.omni.entity.repository.MobileReaderDriverRepository
import kotlinx.coroutines.delay

class MobileReaderDriverRepository :
    MobileReaderDriverRepository {

    var chipDna = ChipDnaDriver()

    override suspend fun getDrivers(): List<MobileReaderDriver> {
        return listOf(chipDna)
    }

    override suspend fun getDriverFor(transaction: Transaction): MobileReaderDriver? {
        if (transaction.source?.contains("CPSDK") == true) {
            return chipDna
        }
        return null
    }

}