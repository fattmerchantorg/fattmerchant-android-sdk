package android

import android.content.Context
import omni.networking.OmniApi

data class InitParams(
    var appContext: Context,
    var apiKey: String,
    var environment: OmniApi.Environment = OmniApi.Environment.LIVE
)