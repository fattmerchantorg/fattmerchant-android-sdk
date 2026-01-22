package com.staxpayments.sample.state

import com.fattmerchant.omni.data.TransactionRequest

/**
 * Tap to Pay configuration modes
 */
enum class TapToPayMode {
    DISABLED,       // External readers only (legacy mode)
    TAP_TO_PAY_ONLY, // NFC only, no external readers
    HYBRID          // Both NFC and external readers
}

/**
 * The UI State class with the StaxViewModel
 * This is only used for logging into the big log view
 */
data class StaxUiState(
    val logString: String = "",
    val tapToPayMode: TapToPayMode = TapToPayMode.DISABLED,
    val transactionStatus: String = "",
    val showTapToPayPrompt: Boolean = false,
    val transactionAmount: String = "0.00",
    val transactionSubtotal: String = "0.00",
    val transactionTip: String = "0.00",
    val transactionRequest: TransactionRequest? = null
)