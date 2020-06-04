package com.fattmerchant.omni.data

import org.junit.Test

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

}