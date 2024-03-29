package com.fattmerchant.omni.usecase

import com.fattmerchant.omni.data.Amount
import com.fattmerchant.omni.data.models.OmniException
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.networking.OmniApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

class CapturePreauthTransactionException(detail: String) : OmniException("Could not get connected mobile reader", detail)

class CapturePreauthTransaction(
    var transactionId: String,
    val omniApi: OmniApi,
    var captureAmount: Amount? = null,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    suspend fun start(failure: (OmniException) -> Unit): Transaction? = coroutineScope {
        /*
        As of 5/21/21, only NMI supports preauth and only NMI is offered to partners so we really
        shouldn't be hitting this code for anything except NMI. With that assumption, we can get
        away with asking the Stax API to perform the capture for us
         */
        val transaction = omniApi.captureTransaction(transactionId, captureAmount) { error ->
            failure(CapturePreauthTransactionException(error.message ?: "Could not capture funds"))
        }

        transaction
    }
}
