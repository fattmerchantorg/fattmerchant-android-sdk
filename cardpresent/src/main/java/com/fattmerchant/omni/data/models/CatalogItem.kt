package com.fattmerchant.omni.data.models

open class CatalogItem: Model {
    override var id: String? = null

    open var item: String? = null
    open var details: String? = null
    open var quantity: Int = 1
    open var price: Double = 0.0

    /** Is the item active */
    open var isActive: Boolean? = null

    /** Is the item taxable */
    open var isTaxable: Boolean? = null

    /** Is the item a service */
    open var isService: Boolean? = null

    /** Is the item a discount */
    open var isDiscount: Boolean? = null

    /** True only when this CatalogItem represents a percentage discount.
     * For example, a 30% off discount */
    open var isPercentage: Boolean? = null

    /** Is the item discountable */
    open var isDiscountable: Boolean? = null
}