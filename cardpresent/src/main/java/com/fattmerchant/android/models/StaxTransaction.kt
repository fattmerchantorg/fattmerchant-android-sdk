package com.fattmerchant.android.models

import com.fattmerchant.android.models.enums.Currency
import com.fattmerchant.android.models.enums.TransactionType
import com.fattmerchant.android.serialization.Utc
import com.squareup.moshi.Json
import java.util.Date

class StaxTransaction(

    @Json(name = "id")
    val id: String? = null,

    @Json(name = "invoice_id")
    val invoiceId: String? = null,

    @Json(name = "reference_id")
    val referenceId: String? = null,

    @Json(name = "recurring_transaction_id")
    val recurringTransactionId: String? = null,

    @Json(name = "auth_id")
    val authId: String? = null,

    @Json(name = "type")
    val type: TransactionType? = null,

    @Json(name = "source")
    val source: String? = null,

    @Json(name = "source_ip")
    val sourceIp: String? = null,

    @Json(name = "is_merchant_present")
    val isMerchantPresent: Boolean? = null,

    @Json(name = "merchant_id")
    val merchantId: String? = null,

    @Json(name = "user_id")
    val userId: String? = null,

    @Json(name = "customer_id")
    val customerId: String? = null,

    @Json(name = "payment_method_id")
    val paymentMethodId: String? = null,

    @Json(name = "is_manual")
    val isManual: Boolean? = null,

    @Json(name = "spreedly_token")
    val spreedlyToken: String? = null,

    @Json(name = "spreedly_response")
    val spreedlyResponse: String? = null,

    @Json(name = "success")
    val success: Boolean? = null,

    @Json(name = "message")
    val message: String? = null,

    @Json(name = "meta")
    val meta: String? = null,

    @Json(name = "total")
    val total: Double? = null,

    @Json(name = "method")
    val method: String? = null,

    @Json(name = "pre_auth")
    val preAuth: Boolean? = null,

    @Json(name = "is_captured")
    val isCaptured: Boolean? = null,

    @Json(name = "last_four")
    val lastFour: String? = null,

    @Json(name = "interchange_code")
    val interchangeCode: String? = null,

    @Json(name = "interchange_fee")
    val interchangeFee: Double? = null,

    @Json(name = "batch_id")
    val batchId: String? = null,

    @Json(name = "batched_at")
    @Utc
    val batchedAt: Date? = null,

    @Json(name = "emv_response")
    val emvResponse: String? = null,

    @Json(name = "avs_response")
    val avsResponse: String? = null,

    @Json(name = "cvv_response")
    val cvvResponse: String? = null,

    @Json(name = "pos_entry")
    val posEntry: String? = null,

    @Json(name = "pos_salesperson")
    val posSalesperson: String? = null,

    @Json(name = "receipt_email_at")
    @Utc
    val receiptEmailAt: Date? = null,

    @Json(name = "receipt_sms_at")
    @Utc
    val receiptSmsAt: Date? = null,

    @Json(name = "settled_at")
    @Utc
    val settledAt: Date? = null,

    @Json(name = "created_at")
    @Utc
    val createdAt: Date? = null,

    @Json(name = "updated_at")
    @Utc
    val updatedAt: Date? = null,

    @Json(name = "gateway_id")
    val gatewayId: String? = null,

    @Json(name = "issuer_auth_code")
    val issuerAuthCode: String? = null,

    @Json(name = "channel")
    val channel: String? = null,

    @Json(name = "currency")
    val currency: Currency? = null
)
