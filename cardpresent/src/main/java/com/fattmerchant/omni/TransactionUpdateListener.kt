package com.fattmerchant.omni

import com.fattmerchant.omni.data.TransactionUpdate

/**
 * Listens to transaction updates
 *
 * This will receive updates like when the transaction starts and when a card is swiped, etc.
 *
 * @see TransactionUpdate
 */
interface TransactionUpdateListener {

    /**
     * Called after a transaction event like card swipe, or signature request
     * @see TransactionUpdate
     */
    fun onTransactionUpdate(transactionUpdate: TransactionUpdate)
}
