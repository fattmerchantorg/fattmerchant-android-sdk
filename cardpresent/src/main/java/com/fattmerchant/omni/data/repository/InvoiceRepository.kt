package com.fattmerchant.omni.data.repository

import com.fattmerchant.omni.data.models.Invoice
import com.fattmerchant.omni.data.models.OmniException

interface InvoiceRepository : ModelRepository<Invoice> {

    class UpdateInvoiceException(message: String? = null) : OmniException("Could not update invoice", message)
    class CreateInvoiceException(message: String? = null) : OmniException("Could not create invoice", message)
    class GetInvoiceException(message: String? = null) : OmniException("Could not get invoices", message)

    override suspend fun create(model: Invoice, error: (OmniException) -> Unit): Invoice? {
        return omniApi.createInvoice(model) {
            error(CreateInvoiceException(it.message))
        }
    }

    override suspend fun update(model: Invoice, error: (OmniException) -> Unit): Invoice? {
        if (model.id == null) {
            error(UpdateInvoiceException("Cannot update invoice with null id"))
            return null
        }
        return omniApi.updateInvoice(model)
    }

    override suspend fun get(error: (OmniException) -> Unit): List<Invoice>? {
        return omniApi.getInvoices {
            error(GetInvoiceException(it.message))
        }
    }

}
