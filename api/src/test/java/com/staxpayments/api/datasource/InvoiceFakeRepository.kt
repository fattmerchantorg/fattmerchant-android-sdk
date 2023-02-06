package com.staxpayments.api.datasource

import com.staxpayments.api.models.Invoice
import com.staxpayments.api.repository.InvoiceRepository
import com.staxpayments.api.requests.CreateInvoiceBody
import java.util.*
import kotlin.collections.LinkedHashMap

class InvoiceFakeRepository: InvoiceRepository {

    private var invoiceData: LinkedHashMap<String, Invoice> = LinkedHashMap()

    override suspend fun getInvoice(invoiceId: String): Invoice? {
        return invoiceData[invoiceId]
    }

    override suspend fun createInvoice(body: CreateInvoiceBody): Invoice {
        val id = UUID.randomUUID().toString()
        val invoice = Invoice(
            id = id,
            total = body.total,
            meta = body.meta,
            status = "PAID",
            url = body.url,
            reminder = null,
            schedule = null,
            customer = null,
            user = null,
            files = body.files,
            childTransactions = emptyList(),
            customerId = body.customerId,
            merchantId = "5f5d4ddf-57a9-421c-9313-31b8d0917269",
            userId = "16212283-da27-4d1e-ab6c-5bc2cd019894",
            isMerchantPresent = true,
            sentAt = null,
            viewedAt = null,
            paidAt = "2023-01-23 00:16:11",
            scheduleId = null,
            reminderId = null,
            paymentMethodId = null,
            isWebPayment = false,
            createdAt = "2023-01-23 00:15:57",
            updatedAt = "2023-01-23 00:16:11",
            deletedAt = null,
            dueAt = null,
            isPartialPaymentEnabled = body.isPartialPaymentEnabled,
            invoiceDateAt = "2023-01-23 00:15:57",
            paymentAttemptFailed = false,
            paymentAttemptMessage = "",
            balanceDue = 0.0,
            totalPaid = 0.09,
            paymentMeta = null
        )

        invoiceData[id] = invoice
        return invoice
    }

    override suspend fun updateInvoice(invoiceId: String, body: CreateInvoiceBody): Invoice? {
        val copyInvoice = invoiceData[invoiceId]?.copy(
            total = body.total,
            meta = body.meta,
            files = body.files,
            customerId = body.customerId,
            isPartialPaymentEnabled = body.isPartialPaymentEnabled,
        )

        return copyInvoice?.let {
            invoiceData[invoiceId] = it
            it
        }
    }
}