package com.fattmerchant.android.chipdna

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Parses information from a chipdna transaction formatted in XML
 */
class ChipDnaXMLTransactionParser {

    companion object {
        /**
         * Parses the expiration date from the XML
         *
         * @param transactionId the ID of the transaction with the payment method in question
         * @param transactionXml an xml-formatted Transaction from TransactionGateway
         */
        fun parseExpirationDate(transactionXml: String, transactionId: String): String? {
            // Create the parser
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(transactionXml))
            var eventType = parser.eventType

            // Tracks the name of the element we are currently parsing
            var currentElementName = ""

            // True when the object being parsed is the transaction that we care to grab the cc_exp from
            var parsingTargetTransaction = false

            // Loop over the document, looking for the cc_exp of the transaction
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {

                    XmlPullParser.START_TAG -> {
                        currentElementName = parser.name
                    }

                    XmlPullParser.TEXT -> {
                        val text = parser.text
                        when (currentElementName) {
                            "transaction_id" -> {
                                if (parser.text == transactionId) {
                                    parsingTargetTransaction = true
                                }
                            }

                            "cc_exp" -> {
                                if (parsingTargetTransaction) {
                                    return text
                                }
                            }
                        }
                    }
                }

                eventType = parser.next()
            }

            return null
        }
    }
}