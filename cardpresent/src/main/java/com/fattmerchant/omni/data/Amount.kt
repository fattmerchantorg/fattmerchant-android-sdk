package com.fattmerchant.omni.data

/**
 * An amount in cents
 *
 * @property cents
 */
data class Amount(
    val cents: Int
) {
    companion object { }

    /**
     * Creates an instance of [Amount] representing the given dollar value
     */
    constructor(dollars: Float) : this(dollars.times(100).toInt())

    /**
     * Creates an instance of [Amount] representing the given dollar value
     */
    constructor(dollars: Double) : this(dollars.toFloat().times(100).toInt())

    /**
     * Creates an instance of [Amount] representing the given dollar value
     */
    constructor(dollars: Number) : this(dollars.toFloat().times(100).toInt())

    /**
     * @return a string that shows how many cents this Amount represents
     */
    fun centsString() = cents.toString()

    /**
     * Creates a string representing the number of dollars that this Amount represents
     *
     * Example: An Amount representing ten dollars would return "10.00"
     *
     * @return a string representing the number of dollars that this Amount represents
     *
     */
    fun dollarsString() = "%.2f".format(cents.toFloat() / 100.0)

    /**
     * @return the amount in dollars
     */
    fun dollars() = dollarsString().toDouble()

    /**
     * Creates a pretty string to represent the amount in dollars
     *
     * Example: ten dollars and eighty-three cents would return "$10.83"
     *
     */
    fun pretty() = "\$${dollarsString()}"
}