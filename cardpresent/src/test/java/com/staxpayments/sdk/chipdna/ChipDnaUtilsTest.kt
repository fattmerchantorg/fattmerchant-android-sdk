package com.staxpayments.sdk.chipdna


import com.creditcall.chipdnamobile.ParameterKeys
import com.staxpayments.android.chipdna.extractUserReference
import com.staxpayments.android.chipdna.withTransactionRequest
import com.staxpayments.sdk.data.Amount
import com.staxpayments.sdk.data.TransactionRequest
import com.staxpayments.sdk.data.models.Transaction
import junit.framework.Assert.assertFalse
import org.junit.Test

class ChipDnaUtilsTest {

    private val amount = Amount(cents = 10)
    private val invoiceId = "InvoiceId"

    @Test
    fun `adds customer vault request parameter if tokenization requested`() {
        val request = TransactionRequest(amount)
        val params = withTransactionRequest(request)
        assert(params.containsKey(ParameterKeys.CustomerVaultCommand))
    }

    @Test
    fun `adds invoice id to request when specified`() {
        val request = TransactionRequest(amount, invoiceId)
        assert(request.invoiceId == invoiceId)
    }

    @Test
    fun `does not add customer vault request parameter if tokenization not requested`() {
        val request = TransactionRequest(amount, false)
        val params = withTransactionRequest(request)
        assertFalse(params.containsKey(ParameterKeys.CustomerVaultCommand))
    }

    @Test
    fun `can extract user reference from transaction`() {
        val userRef = "gotcha!"
        val transaction = Transaction().apply {
            meta = mapOf(
                "nmiUserRef" to userRef
            )
        }

        assert(extractUserReference(transaction) == userRef)
    }

}