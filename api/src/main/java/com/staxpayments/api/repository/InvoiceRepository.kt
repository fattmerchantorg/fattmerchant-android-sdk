package com.staxpayments.api.repository

import com.staxpayments.api.models.Invoice
import com.staxpayments.api.requests.InvoiceRequest

interface InvoiceRepository {

    suspend fun getInvoice(invoiceId: String): Invoice

    suspend fun createInvoice(body: InvoiceRequest): Invoice

    suspend fun updateInvoice(invoiceId: String, body: InvoiceRequest): Invoice
}
