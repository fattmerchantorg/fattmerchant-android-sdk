package com.staxpayments.api.di

import com.staxpayments.api.datasource.CustomerLiveRepository
import com.staxpayments.api.datasource.InvoiceLiveRepository
import com.staxpayments.api.datasource.ItemLiveRepository
import com.staxpayments.api.datasource.UserLiveRepository
import com.staxpayments.api.datasource.MerchantLiveRepository
import com.staxpayments.api.datasource.PaymentMethodLiveRepository
import com.staxpayments.api.datasource.TransactionLiveRepository
import com.staxpayments.api.network.NetworkClient

sealed class Environment {
    object LIVE : Environment()
    object DEV : Environment()
    data class QA(val qaBuildHash: String = "") : Environment()
}

class DataModule {
    var environment: Environment = Environment.LIVE

    private fun baseUrl(): String = when (environment) {
        Environment.LIVE -> "https://apiprod.fattlabs.com/"
        Environment.DEV -> "https://apidev.fattlabs.com/"
        is Environment.QA -> "https://api-qa-${(environment as Environment.QA).qaBuildHash}.qabuilds.fattpay.com/"
    }

    private val networkClients = NetworkClient.initialize(baseUrl())

    init {
        initializeModule()
    }

    private fun initializeModule() {
        UserLiveRepository(networkClients)
        CustomerLiveRepository(networkClients)
        InvoiceLiveRepository(networkClients)
        ItemLiveRepository(networkClients)
        MerchantLiveRepository(networkClients)
        PaymentMethodLiveRepository(networkClients)
        TransactionLiveRepository(networkClients)
    }
}
