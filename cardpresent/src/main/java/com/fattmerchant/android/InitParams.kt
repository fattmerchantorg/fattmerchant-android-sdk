package com.fattmerchant.android

import android.app.Application
import android.content.Context
import com.fattmerchant.omni.networking.OmniApi

/** Contains all the data necessary to initialize [Omni] */
data class InitParams(
    /** The [Context] of your app*/
    var appContext: Context,

    /** The android [Application] */
    var application: Application?,

    /** An ephemeral Omni api key*/
    var apiKey: String,

    /** The Omni environment to use*/
    var environment: OmniApi.Environment = OmniApi.Environment.LIVE,

    /** An id for your application */
    var appId: String = "appid"
)