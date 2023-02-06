package com.staxpayments.api.datasource

import com.staxpayments.api.models.Invoice
import com.staxpayments.api.network.NetworkClient
import com.staxpayments.api.repository.InvoiceRepository
import com.staxpayments.api.requests.CreateInvoiceBody

class InvoiceLiveRepository(
    private val networkClients: NetworkClient
) : InvoiceRepository {

    override suspend fun getInvoice(invoiceId: String): Invoice {
        return networkClients.get("invoice/$invoiceId", responseType = Invoice.serializer())
    }

    override suspend fun createInvoice(body: CreateInvoiceBody): Invoice {
        return networkClients.post("invoice", request = body, responseType = Invoice.serializer())
    }

    override suspend fun updateInvoice(invoiceId: String, body: CreateInvoiceBody): Invoice {
        return networkClients.put("invoice/$invoiceId", request = body, responseType = Invoice.serializer())
    }
}