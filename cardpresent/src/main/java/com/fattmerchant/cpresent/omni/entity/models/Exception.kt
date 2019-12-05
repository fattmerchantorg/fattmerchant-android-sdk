package com.fattmerchant.cpresent.omni.entity.models

import kotlin.Exception

open class OmniException(message: String): Exception() {

    constructor(message: String, detail: String) : this(message) {
        this.detail = detail
    }

    var detail: String? = ""
}