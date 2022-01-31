package com.fattmerchant.omni.data.models

/**
 * An Omni Merchant
 *
 */
class Merchant : Model {
    override var id: String? = null
    var options: Map<String, Any>? = null
    internal var registration: Map<String, Any>? = null

    /**
     * Gets the 'emvTerminalPassword' option from the Merchant
     *
     * The 'emvTerminalId' is used for authenticating with a third-party provider of mobile reader
     */
    fun emvPassword(): String? = getOption("emv_password")

    /**
     * Gets the 'emvTerminalSecret' option from the Merchant
     *
     * The 'emvTerminalSecret' is used for authenticating with a third-party provider of mobile reader
     */
    fun emvTerminalSecret(): String? = getOption("emv_terminal_secret")

    /**
     * Gets the 'emvTerminalSecret' option from the Merchant
     *
     * The 'emvTerminalId' is used for authenticating with a third-party provider of mobile reader
     */
    fun emvTerminalId(): String? = getOption("emv_terminal_id")

    /**
     * Attempts to get a value from [options] at the given [key]
     *
     * @param T the type that the value should be cast to
     * @param key the key where the value is expected to be
     * @return
     */
    private inline fun <reified T> getOption(key: String): T? {
        return try {
            options?.get(key) as? T
        } catch (e: Error) {
            //TODO: Log the error
            null
        }
    }

}