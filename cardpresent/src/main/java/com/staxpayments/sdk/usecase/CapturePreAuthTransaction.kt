package com.staxpayments.sdk.usecase

import com.staxpayments.exceptions.CapturePreAuthTransactionException
import com.staxpayments.exceptions.StaxException
import com.staxpayments.sdk.data.Amount
import com.staxpayments.sdk.data.models.Transaction
import com.staxpayments.sdk.networking.StaxApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

class CapturePreAuthTransaction(
    var transactionId: String,
    val staxApi: StaxApi,
    var captureAmount: Amount? = null,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    suspend fun start(failure: (StaxException) -> Unit): Transaction? = coroutineScope {
        /*
        As of 5/21/21, only NMI supports preauth and only NMI is offered to partners so we really
        shouldn't be hitting this code for anything except NMI. With that assumption, we can get
        away with asking the Stax API to perform the capture for us
         */
        val transaction = staxApi.captureTransaction(transactionId, captureAmount) { error ->
            failure(CapturePreAuthTransactionException(error.message ?: "Could not capture funds"))
        }

        transaction
    }
}
