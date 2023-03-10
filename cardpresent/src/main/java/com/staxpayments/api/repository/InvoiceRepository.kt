package com.staxpayments.api.repository

import com.staxpayments.api.models.Invoice
import com.staxpayments.exceptions.StaxException

internal interface InvoiceRepository : ModelRepository<Invoice> {

    class UpdateInvoiceException(message: String? = null) : StaxException("Could not update invoice", message)
    class CreateInvoiceException(message: String? = null) : StaxException("Could not create invoice", message)
    class GetInvoiceException(message: String? = null) : StaxException("Could not get invoices", message)

    override suspend fun create(model: Invoice, error: (StaxException) -> Unit): Invoice? {
        return staxApi.createInvoice(model) {
            error(CreateInvoiceException(it.message))
        }
    }

    override suspend fun update(model: Invoice, error: (StaxException) -> Unit): Invoice? {
        if (model.id == null) {
            error(UpdateInvoiceException("Cannot update invoice with null id"))
            return null
        }
        return staxApi.updateInvoice(model)
    }

    override suspend fun get(error: (StaxException) -> Unit): List<Invoice>? {
        return staxApi.getInvoices {
            error(GetInvoiceException(it.message))
        }
    }

    override suspend fun getById(id: String, error: (StaxException) -> Unit): Invoice? {
        return staxApi.getInvoice(id) {
            error(GetInvoiceException(it.message))
        }
    }
}
