package com.fattmerchant.cpresent.omni.entity.repository

import com.fattmerchant.cpresent.omni.entity.Amount
import com.fattmerchant.cpresent.omni.entity.models.Invoice
import com.fattmerchant.cpresent.omni.networking.OmniApi
import io.ktor.client.HttpClient
import io.ktor.client.request.get

interface InvoiceRepository: ModelRepository<Invoice> {

    override suspend fun create(model: Invoice, error: (Error) -> Unit): Invoice? = omniApi.createInvoice(model, error)
    override suspend fun update(model: Invoice, error: (Error) -> Unit): Invoice? = omniApi.updateInvoice(model.id!!, model)
    override suspend fun get(error: (Error) -> Unit): List<Invoice>? = omniApi.getInvoices(error)

    /**
     * Creates an Invoice in OmniService with the given total
     */
//    suspend fun createInvoice(total: Amount): Invoice {
//        val invoiceToSave = Invoice().apply {
//            customerId = "8a1471bf-7304-40a3-8d63-0cfe361861a2"
//            this.total = total.dollarsString()
//            meta = mapOf("subtotal" to 1.0)
//            url = "http://127.0.0.1:5432/#/bill/"
//        }
//        return omniApi.createInvoice(invoiceToSave)!!
//    }

}
