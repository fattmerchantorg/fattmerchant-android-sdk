package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Options(
    val social: Social?,
    @SerialName("default_dashboard") val defaultDashboard: String?,
    @SerialName("hosted_payments_url_long") val hostedPaymentsUrlLong: String?,
    @SerialName("hosted_payments_url_short") val hostedPaymentsUrlShort: String?,
    @SerialName("hosted_payments_success_note") val hostedPaymentsSuccessNote: String?,
    @SerialName("hosted_payments_note") val hostedPaymentsNote: String?,
)
