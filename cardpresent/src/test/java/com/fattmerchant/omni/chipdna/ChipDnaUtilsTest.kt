package com.fattmerchant.omni.chipdna


import com.creditcall.chipdnamobile.ParameterKeys
import com.creditcall.chipdnamobile.Parameters
import com.fattmerchant.android.chipdna.extractUserReference
import com.fattmerchant.android.chipdna.withTransactionRequest
import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.models.Transaction
import org.junit.jupiter.api.Test

class ChipDnaUtilsTest {

    private val amount = Amount(cents = 10)
    private val invoiceId = "InvoiceId"

    @Test
    fun `adds customer vault request parameter if tokenization requested`() {
        val request = TransactionRequest(amount)
        val params = Parameters().withTransactionRequest(request)
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
        val params = Parameters().withTransactionRequest(request)
        assert(!params.containsKey(ParameterKeys.CustomerVaultCommand))
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