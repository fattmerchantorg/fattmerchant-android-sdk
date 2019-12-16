package android.chipdna

import com.creditcall.chipdnamobile.Parameters
import omni.data.MobileReader

/**
 * Makes an instance of [MobileReader] for the given [pinPad]
 *
 * @param pinPad
 * @return
 */
fun mapPinPadToMobileReader(pinPad: ChipDnaDriver.SelectablePinPad): MobileReader {
    return object : MobileReader {
        override fun getName() = pinPad.name
    }
}

/**
 * Tries to get the parameter by key.
 *
 * @return null if not found
 */
operator fun Parameters?.get(key: String): String? = this?.getValue(key)