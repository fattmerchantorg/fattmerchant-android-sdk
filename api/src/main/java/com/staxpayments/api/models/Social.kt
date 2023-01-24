package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Social(
    @SerialName("website_url") val websiteUrl: String?,
    @SerialName("facebook_url") val facebookUrl: String?,
    @SerialName("instagram_url") val instagramUrl: String?,
    @SerialName("twitter_url") val twitterUrl: String?,
    @SerialName("linkedin_url") val linkedinUrl: String?
)
