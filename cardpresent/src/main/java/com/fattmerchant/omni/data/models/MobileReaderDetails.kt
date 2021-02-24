package com.fattmerchant.omni.data.models

open class MobileReaderDetails: Model {
    override var id: String? = null
    open var anywhereCommerce: AWCDetails? = null
    open var nmi: NMIDetails? = null

    class AWCDetails {
        var terminalId: String = ""
        var terminalSecret: String = ""
    }

    class NMIDetails {
        var securityKey: String = ""
    }
}

