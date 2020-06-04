package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.TransactionResult
import org.junit.Test

class TakeMobileReaderPaymentTest {

    @Test
    fun `properly adds nmi fields to meta from TransactionResult`() {
        val transactionResult = TransactionResult().apply {
            localId = "localId"
            externalId = "externalId"
            userReference = "userRef"
        }

        val meta = TakeMobileReaderPayment.transactionMetaFrom(transactionResult)

        assert(meta["nmiUserRef"] == "userRef")
        assert(meta["cardEaseReference"] == "localId")
        assert(meta["nmiTransactionId"] == "externalId")
    }
}