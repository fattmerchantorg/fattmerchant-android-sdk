package com.staxpayments.android.chipdna

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Parses information from a ChipDna transaction formatted in XML
 */
internal class ChipDnaXMLTransactionParser {

    companion object {
        /**
         * Parses the expiration date from the XML
         * @param xml an xml-formatted Transaction from TransactionGateway
         * @param id the ID of the transaction with the payment method in question
         */
        fun parseExpirationDate(xml: String, id: String): String? {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true

            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType

            // Tracks the name of the element we are currently parsing
            var currentElementName = ""

            // True when the object being parsed is the transaction that we care to grab the cc_exp from
            var isParsingTargetTransaction = false

            // Loop over the document, looking for the cc_exp of the transaction
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentElementName = parser.name
                    }

                    XmlPullParser.TEXT -> {
                        when (currentElementName) {
                            "transaction_id" -> {
                                if (parser.text == id) {
                                    isParsingTargetTransaction = true
                                }
                            }

                            "cc_exp" -> {
                                if (isParsingTargetTransaction) {
                                    return parser.text
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
