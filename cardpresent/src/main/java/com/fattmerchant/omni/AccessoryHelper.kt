package com.fattmerchant.omni

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log

class AccessoryHelper(
    private val context: Context,
    private val listener: UsbAccessoryListener
) {

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_ACCESSORY_ATTACHED -> {
                    val accessory: UsbAccessory? = getUsbAccessoryExtra(intent)
                    accessory?.let { listener.onUsbAccessoryConnected() }
                }
                UsbManager.ACTION_USB_ACCESSORY_DETACHED -> {
                    val accessory: UsbAccessory? = getUsbAccessoryExtra(intent)
                    accessory?.let { listener.onUsbAccessoryDisconnected() }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device: UsbDevice? = getUsbDeviceExtra(intent)
                    device?.let { d ->
                        val manufacturer = d.manufacturerName ?: ""
                        if (manufacturer.contains("id tech", true)) {
                            listener.onUsbAccessoryConnected()
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device: UsbDevice? = getUsbDeviceExtra(intent)
                    device?.let { d ->
                        val manufacturer = d.manufacturerName ?: ""
                        if (manufacturer.contains("id tech", true)) {
                            listener.onUsbAccessoryDisconnected()
                        }
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
            addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        context.registerReceiver(usbReceiver, filter)
    }

    fun unregister() {
        context.unregisterReceiver(usbReceiver)
    }

    private fun getUsbAccessoryExtra(intent: Intent): UsbAccessory? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY, UsbAccessory::class.java)
        } else {
            // Not Deprecated in Android 12 and below
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY)
        }
    }

    private fun getUsbDeviceExtra(intent: Intent): UsbDevice? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            // Not Deprecated in Android 12 and below
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }
    }
}