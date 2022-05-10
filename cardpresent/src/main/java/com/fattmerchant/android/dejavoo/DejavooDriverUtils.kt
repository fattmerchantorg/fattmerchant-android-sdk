package com.fattmerchant.android.dejavoo

import com.dvmms.dejapay.models.DejavooTransactionResponse
import com.fattmerchant.omni.data.models.DejavooTerminalCredentials
import com.fattmerchant.omni.data.models.Merchant

class DejavooDriverUtils {

    internal fun getCurrentTerminalCreds(merchant: Merchant): DejavooTerminalCredentials? {
        val currentSerialNumber = getCurrentSerialNumber() ?: return null

        return try {
            val mid = merchant.registration?.get("merchant") as? Map<String, Any> ?: return null
            val vendorKeys = mid["vendor_keys"] as? Map<String, Any> ?: return null
            val terminals = vendorKeys["terminals"] as? List<Map<String, Any>> ?: return null

            var matchingCreds: DejavooTerminalCredentials? = null

            for (terminal in terminals) {
                val type = terminal["type"] as? String ?: continue
                val serial = terminal["serial"] as? String ?: continue
                val tpn = terminal["tpn"] as? String ?: continue
                val key = terminal["key"] as? String ?: continue
                val nickname = terminal["nickname"] as? String // optional

                // Register id is status_port for some weird reason
                val registerId = terminal["status_port"] as? String ?: continue

                // Make sure it's dejavoo
                if (type != "dejavoo") { continue }

                // Make sure it has a matching serial
                if (serial != currentSerialNumber) { continue }

                matchingCreds = DejavooTerminalCredentials (
                    key = key,
                    serial = serial,
                    registerId = registerId,
                    tpn,
                    nickname
                )
            }

            return matchingCreds
        } catch (e: Error) {
            null
        }
    }

    /** Gets the serial number for the device */
    internal fun getCurrentSerialNumber(): String? {
        /*
        As of Jan 2022, we understand that this method is deprecated. However, given the constraints
        under which we will _actually_ need this method, it works just fine.

        android.os.Build.SERIAL works on Android 8, and the Dejavoo devices all run android 8. If
        Dejavoo ever upgrades these devices to Android 9, we will need to start using the new APIs
        for getting serial numbers. We are aware of this and will be waiting to hear from Dejavoo in
        case that happens.
         */
        return android.os.Build.SERIAL
    }

    companion object {
        internal fun gatewayResponse(response: DejavooTransactionResponse): MutableMap<String, Any?> {
            return mutableMapOf(
                    "RegisterId" to response.registerId,
                    "AuthCode" to response.authenticationCode,
                    "message" to response.responseMessage,
                    "PNRef" to response.pnReference,
                    "PaymentType" to response.paymentType.toString(),
                    "RegisterId" to response.registerId,
                    "RespMSG" to response.responseMessage,
                    "SN" to response.serialNumber,
                    "TransType" to response.transactionType.toString(),
                    "Voided" to response.isVoided,
                    "paymentType" to response.paymentType.toString(),
                    "refId" to response.referenceId,
                    "registerId" to response.registerId,
                    "registerSerial" to response.serialNumber,
                    "registerType" to "dejavoo",
                    "transactionType" to response.transactionType.toString(),
                    "type" to response.paymentType.toString()
            )
        }
    }
}