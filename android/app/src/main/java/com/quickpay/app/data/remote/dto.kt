package com.quickpay.app.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateLinkReq(
    val amountCents: Long,          // <-- Long to match backend BIGINT
    val currency: String,
    val description: String? = null,
    val note: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateLinkRes(
    val orderId: String,
    val url: String,
    val status: String
)

//can delete, not using
@JsonClass(generateAdapter = true)
data class PaymentLinkDto(
    val id: String,
    val amountCents: Long,
    val currency: String,
    val description: String?,
    val checkoutUrl: String?,       // null until Finix is wired
    val status: String              // PENDING | PAID | CANCELED | EXPIRED (server)
)

@JsonClass(generateAdapter = true)
data class OrderDto(
    val id: String,
    val linkId: String,
    val status: String,             // CREATED | AUTHORIZED | CAPTURED | FAILED (server)
    val amountCents: Long,
    val currency: String,
    val note: String? = null
)