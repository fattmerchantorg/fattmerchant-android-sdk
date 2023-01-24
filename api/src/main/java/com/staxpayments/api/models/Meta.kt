package com.staxpayments.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    val mime: String?,
    val extension: String?,
    val size: Size?,
    @SerialName("filesize_bytes") val fileSizeBytes: Long?,
    @SerialName("filesize") val fileSize: String?,
)
