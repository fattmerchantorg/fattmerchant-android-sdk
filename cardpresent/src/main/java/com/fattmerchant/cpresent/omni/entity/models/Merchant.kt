package com.fattmerchant.cpresent.omni.entity.models

class Merchant: Model {
    override var id: String? = null
    var options: Map<String, Any>? = null

    /**
     * Use as the apiKey for mobile reader
     *
     * @return
     */
    fun emvPassword(): String? = getOption("emv_password")

    private inline fun <reified T>getOption(key: String): T? {
        return try {
            options?.get(key) as? T
        } catch (e: Error) {
            //TODO: Log the error
            null
        }
    }

}