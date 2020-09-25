package com.fattmerchant.omni.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AmountTest {

    @Test
    fun centsString() {
        assertEquals(Amount(50).centsString(), "50")
        assertEquals(Amount(50.30).centsString(), "5030")
        assertEquals(Amount(50.30f).centsString(), "5030")
        assertEquals(Amount(50.309).centsString(), "5030")
        assertEquals(Amount(0.09).centsString(), "9")
    }

    @Test
    fun dollarsString() {
        assertEquals(Amount(50).dollarsString(), "0.50")
        assertEquals(Amount(50.30).dollarsString(), "50.30")
        assertEquals(Amount(50.30f).dollarsString(), "50.30")
        assertEquals(Amount(50.309).dollarsString(), "50.30")
        assertEquals(Amount(0.09).dollarsString(), "0.09")
    }

    @Test
    fun pretty() {
        assertEquals(Amount(50).pretty(), "$0.50")
        assertEquals(Amount(50.30).pretty(), "$50.30")
        assertEquals(Amount(50.30f).pretty(), "$50.30")
        assertEquals(Amount(50.309).pretty(), "$50.30")
        assertEquals(Amount(0.09).pretty(), "$0.09")
    }

    @Test
    fun dollars() {
        assertEquals(Amount(50).dollars(), 0.50)
        assertEquals(Amount(50.30).dollars(), 50.30)
        assertEquals(Amount(50.30f).dollars(), 50.30)
        assertEquals(Amount(50.309).dollars(), 50.30)
        assertEquals(Amount(0.09).dollars(), 0.09)
    }

    @Test
    fun getCents() {
        assertEquals(Amount(50).cents, 50)
        assertEquals(Amount(50.30).cents, 5030)
        assertEquals(Amount(50.30f).cents, 5030)
        assertEquals(Amount(50.309).cents, 5030)
        assertEquals(Amount(0.09).cents, 9)
    }
}