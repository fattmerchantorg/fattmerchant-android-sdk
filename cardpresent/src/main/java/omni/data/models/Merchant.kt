package omni.data.models

/**
 * An Omni Merchant
 *
 */
class Merchant : Model {
    override var id: String? = null
    var options: Map<String, Any>? = null

    /**
     * Use as the apiKey for mobile reader
     *
     * @return
     */
    fun emvPassword(): String? = getOption("emv_password")

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