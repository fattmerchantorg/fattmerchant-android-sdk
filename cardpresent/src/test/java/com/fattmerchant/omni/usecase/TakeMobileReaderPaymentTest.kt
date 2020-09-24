package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.TransactionResult
import com.fattmerchant.omni.data.models.CatalogItem
import org.junit.jupiter.api.Test

class TakeMobileReaderPaymentTest {

    @Test
    fun `properly adds nmi fields to meta from TransactionResult`() {

        val transactionResult = TransactionResult().apply {
            localId = "localId"
            externalId = "externalId"
            userReference = "userRef"
            source = "NMI"
        }

        val meta = TakeMobileReaderPayment.transactionMetaFrom(transactionResult)

        assert(meta["nmiUserRef"] == "userRef")
        assert(meta["cardEaseReference"] == "localId")
        assert(meta["nmiTransactionId"] == "externalId")
    }

    @Test
    fun `properly adds catalog items to meta from TransactionResult`() {
        val testItem1 = CatalogItem().apply {
            id = "fakeid1"
            item = "Test Item 1"
            details = "The first test item."
            quantity = 1
            price = 0.1
        }

        val testItem2 = CatalogItem().apply {
            id = "fakeid2"
            item = "Test Item 2"
            details = "The second test item."
            quantity = 2
            price = 0.1
        }

        val testItems = listOf(testItem1, testItem2)

        val expectedMeta = mutableMapOf<String, Any>()

        testItems.let {
            expectedMeta["lineItems"] = it
        }

        val transactionRequest = TransactionRequest(Amount(cents = 3), testItems)

        val transactionResult = TransactionResult().apply {
            request = transactionRequest
        }

        val meta = TakeMobileReaderPayment.transactionMetaFrom(transactionResult)

        assert(meta["lineItems"] == expectedMeta["lineItems"])
    }

    @Test
    fun `properly adds memo, reference, tip, tax, subtotal to meta from TransactionResult`() {
        val transactionResult = TransactionResult().apply {
            localId = "localId"
            externalId = "externalId"
            userReference = "userRef"
            source = "NMI"
            request = TransactionRequest(Amount(50.0)).apply {
                subtotal = 40.0
                tax = 6.0
                tip = 4.0
                memo = "This is a memo"
                reference = "this is a reference"
            }
        }

        val meta = TakeMobileReaderPayment.transactionMetaFrom(transactionResult)

        assert(meta["subtotal"] == 40.0)
        assert(meta["tax"] == 6.0)
        assert(meta["tip"] == 4.0)
        assert(meta["reference"] == "this is a reference")
        assert(meta["memo"] == "This is a memo")
    }
}