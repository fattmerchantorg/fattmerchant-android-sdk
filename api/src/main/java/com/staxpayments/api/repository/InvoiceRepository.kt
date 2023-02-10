package com.staxpayments.api.repository

import com.staxpayments.api.models.Invoice

interface InvoiceRepository {

    suspend fun getInvoice(invoiceId: String): Invoice

    suspend fun createInvoice(body: Invoice): Invoice

    suspend fun updateInvoice(invoiceId: String, body: Invoice): Invoice
}
