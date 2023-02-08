package com.staxpayments.api.repository

import com.staxpayments.api.models.Invoice
import com.staxpayments.api.requests.InvoicePostRequest

interface InvoiceRepository {

    suspend fun getInvoice(invoiceId: String): Invoice

    suspend fun createInvoice(body: InvoicePostRequest): Invoice

    suspend fun updateInvoice(invoiceId: String, body: InvoicePostRequest): Invoice
}
