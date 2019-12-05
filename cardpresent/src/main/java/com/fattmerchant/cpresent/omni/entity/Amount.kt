package com.fattmerchant.cpresent.omni.entity

data class Amount(
    val cents: Int
) {
    constructor(dollars: Float): this(dollars.times(100).toInt())
    constructor(dollars: Double): this(dollars.times(100).toInt())
    constructor(dollars: Number): this(dollars.toFloat().times(100).toInt())

    fun centsString() = cents.toString()
    fun dollarsString() = (cents.toFloat() / 100.0).toString()
}