package com.staxpayments.api.repository

import com.staxpayments.api.models.Invoice
import com.staxpayments.api.requests.CreateInvoiceBody

interface InvoiceRepository {

    suspend fun getInvoice(invoiceId: String): Invoice?

    suspend fun createInvoice(body: CreateInvoiceBody): Invoice

    suspend fun updateInvoice(invoiceId: String, body: CreateInvoiceBody): Invoice?
}