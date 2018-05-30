package com.fattmerchant.tokenization.networking

/** Configures the FattmerchantApi */
public class FattmerchantConfiguration (

        /** Base URL of the Fattmerchant API */
        var baseUrl: String = "https://apiprod01.fattlabs.com",

        /** ID given by Fattmerchant that allows you to create tokenized payment methods */
        var webPaymentsToken: String = "changeme"
) {
    companion object { var shared: FattmerchantConfiguration = FattmerchantConfiguration() }
}
