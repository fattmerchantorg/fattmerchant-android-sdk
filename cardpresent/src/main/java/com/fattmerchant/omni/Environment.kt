package com.fattmerchant.omni

sealed class Environment {
    object LIVE : Environment()
    object DEV : Environment()
    data class QA(val qaBuildHash: String = "") : Environment()
}
