package com.fattmerchant.omni

interface UsbAccessoryListener {
    /// Called when a something is plugged into the iPhone/iPad
    fun onUsbAccessoryConnected()

    /// Called when a something is unplugged into the iPhone/iPad
    fun onUsbAccessoryDisconnected()
}