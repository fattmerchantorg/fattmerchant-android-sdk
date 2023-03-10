package com.staxpayments.android.chipdna

import com.creditcall.chipdnamobile.ChipDnaMobileSerializer
import com.creditcall.chipdnamobile.IConfigurationUpdateListener
import com.creditcall.chipdnamobile.IDeviceUpdateListener
import com.creditcall.chipdnamobile.ParameterKeys
import com.creditcall.chipdnamobile.Parameters
import com.staxpayments.sdk.MobileReaderConnectionStatus
import com.staxpayments.sdk.MobileReaderConnectionStatusListener

class ChipDnaDeviceListener(private val listener: MobileReaderConnectionStatusListener? = null) :
    IConfigurationUpdateListener,
    IDeviceUpdateListener
{
    override fun onConfigurationUpdateListener(parameters: Parameters?) {
        parameters[ParameterKeys.ConfigurationUpdate]?.let {
            MobileReaderConnectionStatus.from(it)?.let {
                listener?.mobileReaderConnectionStatusUpdate(it)
            }
        }
    }

    override fun onDeviceUpdate(parameters: Parameters?) {
        parameters[ParameterKeys.DeviceStatusUpdate]?.let { deviceStatusXml ->
            ChipDnaMobileSerializer.deserializeDeviceStatus(deviceStatusXml)?.let { deviceStatus ->
                MobileReaderConnectionStatus.from(deviceStatus.status)?.let {
                    listener?.mobileReaderConnectionStatusUpdate(it)
                }
            }
        }
    }
}