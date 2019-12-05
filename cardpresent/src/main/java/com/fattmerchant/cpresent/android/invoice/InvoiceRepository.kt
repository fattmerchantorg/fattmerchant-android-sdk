//package com.fattmerchant.cpresent.android.invoice
//
//import com.fattmerchant.cpresent.android.api.OmniApi
//import com.fattmerchant.cpresent.omni.entity.Amount
//import com.fattmerchant.cpresent.omni.entity.repository.InvoiceRepository
//import com.fattmerchant.cpresent.omni.entity.models.Invoice as OmniInvoice
//import timber.log.Timber
//
//class InvoiceRepository(
//    val omniApi: OmniApi
//){
//
//    override suspend fun create(model: OmniInvoice): OmniInvoice {
//        try {
//            val createdInvoice = omniApi.createInvoice(
//                Invoice.fromOmniInvoice(model)
//            )
//            Timber.i(createdInvoice.toString())
//            return createdInvoice
//        } catch (e: Throwable) {
//            Timber.e(e)
//        }
//
//        return OmniInvoice()
//    }
//
//    override suspend fun update(model: OmniInvoice): OmniInvoice {
//        try {
//            val updatedInvoice = omniApi.updateInvoice(
//                model.id!!,
//                Invoice.fromOmniInvoice(model)
//            )
//            Timber.i(updatedInvoice.toString())
//            return updatedInvoice
//        } catch (e: Throwable) {
//            Timber.e(e)
//        }
//
//        return OmniInvoice()
//    }
//
//    override suspend fun delete(model: OmniInvoice): Boolean {
//        return true
//    }
//
//    override suspend fun getById(id: String): OmniInvoice {
//        return Invoice().apply { this.id = id }
//    }
//
//    override suspend fun createInvoice(total: Amount): OmniInvoice {
//
//        val invoiceToSave = Invoice().apply {
//            customerId = "8a1471bf-7304-40a3-8d63-0cfe361861a2"
//            this.total = total.dollarsString()
//            meta = mapOf("subtotal" to 1.0)
//            url = "http://127.0.0.1:5432/#/bill/"
//        }
//
//        try {
//            val createdInvoice = omniApi.createInvoice(invoiceToSave)
//            Timber.i(createdInvoice.toString())
//            return createdInvoice
//        } catch (e: Throwable) {
//            Timber.e(e)
//        }
//
//        return Invoice().apply {
//            id = "12345"
//            this.total = total.dollarsString()
//        }
//    }
//
//}