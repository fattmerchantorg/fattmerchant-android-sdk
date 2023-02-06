package com.staxpayments.api.repository

import com.staxpayments.api.models.Invoice
import com.staxpayments.api.requests.CreateInvoiceBody

interface InvoiceRepository {

    // READ
    suspend fun getInvoice(invoiceId: String): Invoice?

    // WRITE
    suspend fun createInvoice(body: CreateInvoiceBody): Invoice

    // UPDATE
    suspend fun updateInvoice(invoiceId: String, body: CreateInvoiceBody): Invoice?
}