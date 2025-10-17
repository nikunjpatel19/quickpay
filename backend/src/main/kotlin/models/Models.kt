package com.quickpay.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkReq(val amountCents: Long, val currency: String = "USD", val note: String? = null)

@Serializable
data class PaymentLinkDto(
    val id: String,
    val amountCents: Long,
    val currency: String,
    val status: String,
    val qr: String
)
