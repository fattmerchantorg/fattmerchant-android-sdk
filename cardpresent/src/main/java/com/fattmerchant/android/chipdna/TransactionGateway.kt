package com.fattmerchant.android.chipdna

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.content.MultiPartData

/**
 * Communicates with Transaction Gateway via the Direct Post API
 *
 * https://fattmerchant.transactiongateway.com/merchants/resources/integration/integration_portal.php
 */
internal class TransactionGateway {
    companion object {
        /** Transaction Gateway's baseUrl */
        val baseUrl = "https://secure.nmi.com/api/query.php"

        /**
         * Fetches the expiration date of the card used to run the transaction with the given id
         *
         * @param securityKey authenitcation for TransactionGateway
         * @param transactionId the id of the Transaction within NMI
         * @return the expiration date of the card in the 'mmyy' format if found. Null otherwise
         */
        suspend fun getTransactionCcExpiration(securityKey: String, transactionId: String): String? {
            val client = HttpClient {}
            val response = client.get<String>(urlString = "$baseUrl?security_key=$securityKey&transaction_id=$transactionId")
            return ChipDnaXMLTransactionParser.parseExpirationDate(response, transactionId)
        }
    }
}