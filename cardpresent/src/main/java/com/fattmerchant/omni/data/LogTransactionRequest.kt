package com.fattmerchant.omni.data

import com.fattmerchant.omni.data.models.Customer
import com.fattmerchant.omni.data.models.Invoice
import com.fattmerchant.omni.data.models.PaymentMethod
import com.fattmerchant.omni.data.models.Transaction

/**
 * A request to log a transaction
 */
internal open class LogTransactionRequest {

    /**
     * The customer that is to be assigned to the transaction
     *
     * This is mutually exclusive to the customer_id field. If one exists, the other must not
     * */
    var customer: Customer? = null

    /**
     * The id of the customer to assign to the transaction
     *
     * This is mutually exclusive to the customer field. If one exists, the other must not
     * */
    var customerId: String? = null

    /**
     * The invoice that is to be assigned to the transaction
     *
     * This is mutually exclusive to the invoice_id field. If one exists, the other must not
     * */
    var invoice: Invoice? = null

    /**
     * The id of the invoice that is to be assigned to the transaction
     *
     * This is mutually exclusive to the invoice field. If one exists, the other must not
     * */
    var invoiceId: String? = null

    /** The PaymentMethod that is to be assigned to the transaction */
    var paymentMethod: PaymentMethod

    /** The details about the transaction */
    var transaction: Transaction

    constructor(transaction: Transaction, paymentMethod: PaymentMethod, invoice: Invoice, customer: Customer) {
        this.transaction = transaction
        this.invoice = invoice
        this.paymentMethod = paymentMethod
        this.customer = customer
    }

    constructor(transaction: Transaction, paymentMethod: PaymentMethod, invoiceId: String, customer: Customer) {
        this.transaction = transaction
        this.invoiceId = invoiceId
        this.paymentMethod = paymentMethod
        this.customer = customer
    }

    constructor(transaction: Transaction, paymentMethod: PaymentMethod, invoice: Invoice, customerId: String) {
        this.transaction = transaction
        this.invoice = invoice
        this.paymentMethod = paymentMethod
        this.customerId = customerId
    }

    constructor(transaction: Transaction, paymentMethod: PaymentMethod, invoiceId: String, customerId: String) {
        this.transaction = transaction
        this.invoiceId = invoiceId
        this.paymentMethod = paymentMethod
        this.customerId = customerId
    }

}
