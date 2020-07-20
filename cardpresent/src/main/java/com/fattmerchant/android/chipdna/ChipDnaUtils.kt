package com.fattmerchant.android.chipdna

import android.bluetooth.BluetoothClass
import com.creditcall.chipdnamobile.DeviceStatus
import com.creditcall.chipdnamobile.ParameterKeys
import com.creditcall.chipdnamobile.ParameterValues
import com.creditcall.chipdnamobile.Parameters
import com.creditcall.chipdnamobile.TransactionUpdate as ChipDnaTransactionUpdate
import com.fattmerchant.android.chipdna.ChipDnaDriver
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.TransactionRequest
import com.fattmerchant.omni.data.TransactionUpdate
import com.fattmerchant.omni.data.models.MobileReaderConnectionStatus
import com.fattmerchant.omni.data.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

/**
 * Makes an instance of [MobileReader] for the given [pinPad]
 *
 * @param pinPad a pin pad that can be connected
 * @return a [MobileReader]
 */
internal fun mapPinPadToMobileReader(pinPad: ChipDnaDriver.SelectablePinPad): MobileReader {
    return object : MobileReader {
        override fun getName() = pinPad.name
        override fun getFirmwareVersion(): String? = null
        override fun getMake(): String? = null
        override fun getModel(): String? = null
        override fun serialNumber(): String? = null
    }
}

/**
 * Makes a [MobileReader] from the given [DeviceStatus]
 *
 * @param deviceStatus a [DeviceStatus] object that ChipDna uses to represent a device
 * @return a [MobileReader]
 */
internal fun mapDeviceStatusToMobileReader(deviceStatus: DeviceStatus): MobileReader {
    return object : MobileReader {
        override fun getName() = deviceStatus.name
        override fun getFirmwareVersion(): String? = deviceStatus.firmwareVersion
        override fun getMake(): String? = deviceStatus.make
        override fun getModel(): String? = deviceStatus.model
        override fun serialNumber(): String? = deviceStatus.serialNumber
    }
}

/**
 * Maps a ChipDna TransactionUpdate to an Omni Transaction Update
 *
 * @param transactionUpdate the value of a ChipDnaTransactionUpdate
 * @return an Omni [TransactionUpdate]
 */
fun mapTransactionUpdate(transactionUpdate: String): TransactionUpdate? {
    return when (transactionUpdate) {
        ChipDnaTransactionUpdate.CardEntryPrompted.value -> {
            return if (ChipDnaDriver.getConnectedReader()?.getMake() == ChipDnaDriver.Companion.PinPadManufacturer.Miura.name) {
                TransactionUpdate.PromptInsertSwipeCard
            } else {
                TransactionUpdate.PromptSwipeCard
            }
        }

        ChipDnaTransactionUpdate.CardSwiped.value -> TransactionUpdate.CardSwiped
        ChipDnaTransactionUpdate.SmartcardInserted.value -> TransactionUpdate.CardInserted
        ChipDnaTransactionUpdate.CardSwipeError.value -> TransactionUpdate.CardSwipeError
        ChipDnaTransactionUpdate.SmartcardRemovePrompted.value -> TransactionUpdate.PromptRemoveCard
        ChipDnaTransactionUpdate.SmartcardRemoved.value -> TransactionUpdate.CardRemoved
        else -> null
    }
}

/**
 * Tries to get the parameter by key.
 *
 * @return null if not found
 */
operator fun Parameters?.get(key: String): String? = this?.getValue(key)

/**
 * The param value to make NMI add a customer to the customer vault
 *
 * This should be used with the ParameterKeys.CustomerVaultCommand and passed into startTransaction()
 */
val ParameterValuesAddCustomer = "add-customer"

/**
 * Gets the User Reference from the given [Transaction]
 *
 * @param transaction
 * @return a string containing the user reference or null if not found
 */
internal fun extractUserReference(transaction: Transaction): String? =
        (transaction.meta as? Map<*, *>)?.get("nmiUserRef") as? String

/**
 * Gets the Card Ease Reference from the given [Transaction]
 *
 * @param transaction
 * @return a string containing the user reference or null if not found
 */
internal fun extractCardEaseReference(transaction: Transaction): String? =
        (transaction.meta as? Map<*, *>)?.get("cardEaseReference") as? String


/**
 * Generates a user reference for chipDNA transactions
 *
 * @return String containing the generated user reference
 */
internal fun generateUserReference(): String =
        String.format("CDM-%s", SimpleDateFormat("yy-MM-dd-HH.mm.ss", Locale.US).format(Date()))

internal fun Parameters.withTransactionRequest(request: TransactionRequest) = Parameters().apply {
    add(ParameterKeys.Amount, request.amount.centsString())
    add(ParameterKeys.AmountType, ParameterValues.AmountTypeActual)
    add(ParameterKeys.Currency, "USD")
    add(ParameterKeys.UserReference, generateUserReference())
    add(ParameterKeys.PaymentMethod, ParameterValues.Card)
    add(ParameterKeys.AutoConfirm, ParameterValues.TRUE)
    add(ParameterKeys.TransactionType, ParameterValues.Sale)

    if (request.tokenize) {
        add(ParameterKeys.CustomerVaultCommand, ParameterValuesAddCustomer)
    }
}

/**
 * Initializes a `MobileReaderConnectionStatus` object from the given ChipDnaConfigurationUpdate
 *
 * ChipDna hands us a ton of configuration updates during the mobile reader connection process
 * The ones we care about are:
 *  - Connecting
 *  - Permorming Tms Update
 *  - Updating Pinpad Firmware
 *  - Rebooting
 *  - Registering
 *
 * @param chipDnaConfigurationUpdate
 */
fun MobileReaderConnectionStatus.Companion.from(chipDnaConfigurationUpdate: String): MobileReaderConnectionStatus? = when(chipDnaConfigurationUpdate) {
    ParameterValues.Connecting -> MobileReaderConnectionStatus.CONNECTING

    ParameterValues.UpdatingPinPadFirmware,
    ParameterValues.PerformingTmsUpdate,
    ParameterValues.Registering -> MobileReaderConnectionStatus.UPDATING

    ParameterValues.RebootingPinPad -> MobileReaderConnectionStatus.REBOOTING

    else -> null
}

fun MobileReaderConnectionStatus.Companion.from(chipDnaDeviceStatus: DeviceStatus.DeviceStatusEnum) = when(chipDnaDeviceStatus) {
    DeviceStatus.DeviceStatusEnum.DeviceStatusDisconnected -> MobileReaderConnectionStatus.DISCONNECTED
    else -> null
}