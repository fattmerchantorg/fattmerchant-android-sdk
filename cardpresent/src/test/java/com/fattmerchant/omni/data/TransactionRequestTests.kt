package com.fattmerchant.omni.data

import org.junit.jupiter.api.Test

class TransactionRequestTests {

    @Test
    fun `transaction request defaults to tokenization enabled`() {
        // Create a TransactionRequest but don't pass in tokenization
        val request = TransactionRequest(Amount(10))
        assert(request.tokenize)
    }

    @Test
    fun `can initialize with or without tokenize param`() {
        TransactionRequest(Amount(10), true)
        TransactionRequest(Amount(10))
        assert(true)
    }

    @Test
    fun `can initialize with or without metadata`() {
        TransactionRequest(Amount(10), true)
        TransactionRequest(Amount(10))
        assert(true)

        val request = TransactionRequest(
                amount = Amount(10),
                memo = "1",
                subtotal = 2.0,
                tip = 3.0,
                tax = 4.0,
                reference = "5"
        )

        assert(request.memo == "1")
        assert(request.subtotal == 2.0)
        assert(request.tip == 3.0)
        assert(request.tax == 4.0)
        assert(request.reference == "5")
    }

}