package com.fattmerchant.omni.data.models

open class CatalogItem: Model {
    override var id: String? = null

    open var item: String? = null
    open var details: String? = null
    open var quantity: Int = 1
    open var price: Double = 0.0
}