package com.fattmerchant.android

import android.os.Build
import com.fattmerchant.android.Mock.MockDriver
import com.fattmerchant.android.anywherecommerce.AWCDriver
import com.fattmerchant.android.chipdna.ChipDnaDriver
import com.fattmerchant.android.dejavoo.DejavooDriver
import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.MobileReaderDriver
import com.fattmerchant.omni.data.PaymentTerminalDriver
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository

internal class MobileReaderDriverRepository : MobileReaderDriverRepository {

    private val isRunningOnEmulator: Boolean by lazy {
        // Android SDK emulator
        return@lazy ((Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                && Build.FINGERPRINT.endsWith(":user/release-keys")
                && Build.MANUFACTURER == "Google" && Build.PRODUCT.startsWith("sdk_gphone_") && Build.BRAND == "google"
                && Build.MODEL.startsWith("sdk_gphone_"))
                //
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                //bluestacks
                || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(
            Build.MANUFACTURER,
            ignoreCase = true
        ) //bluestacks
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HOST.startsWith("Build") //MSI App Player
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT == "google_sdk")
    }

    private var chipDna = ChipDnaDriver()
    private var awc = AWCDriver()
    private var dejavooTerminal = DejavooDriver()
    private var mockDriver = MockDriver()


    override suspend fun getDrivers(): List<MobileReaderDriver> {
        if (isRunningOnEmulator) {
            return listOf(mockDriver)
        }
        return listOf(awc, chipDna)
    }

    override suspend fun getInitializedDrivers(): List<MobileReaderDriver> {
        return getDrivers().filter { it.isInitialized() }
    }

    override suspend fun getDriverFor(transaction: Transaction): MobileReaderDriver? {
        if (transaction.source?.contains("CPSDK") != true) {
            return null
        }

        getInitializedDrivers().forEach {
            if (transaction.source?.contains(it.source) == true) {
                return it
            }
        }

        return null
    }

    override suspend fun getDriverFor(mobileReader: MobileReader): MobileReaderDriver? {
        mobileReader.serialNumber()?.let { serial ->
            return getInitializedDrivers().firstOrNull {
                it.familiarSerialNumbers.contains(serial)
            }
        } ?: return null
    }

    override fun getTerminal(): PaymentTerminalDriver? {
        return dejavooTerminal
    }
}