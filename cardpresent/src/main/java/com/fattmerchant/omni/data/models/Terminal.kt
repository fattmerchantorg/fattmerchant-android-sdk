package com.fattmerchant.omni.data.models

internal data class DejavooTerminalCredentials(
    val key: String,
    val serial: String,
    val registerId: String,
    val tpn: String,
    val nickname: String?
)