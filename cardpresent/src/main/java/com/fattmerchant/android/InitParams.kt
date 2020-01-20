package com.fattmerchant.android

import android.content.Context
import com.fattmerchant.omni.networking.OmniApi

data class InitParams(
    var appContext: Context,
    var apiKey: String,
    var environment: OmniApi.Environment = OmniApi.Environment.LIVE
)