package com.staxpayments.android

import android.app.Application
import android.content.Context
import com.staxpayments.sdk.Environment

/** Contains all the data necessary to initialize [Stax] */
data class InitParams(
    /** The [Context] of your app*/
    var appContext: Context,

    /** The android [Application] */
    var application: Application?,

    /** An ephemeral Stax api key*/
    var apiKey: String,

    /** The Stax environment to use*/
    var environment: Environment = Environment.LIVE,

    /** An id for your application */
    var appId: String = "appid"
)
