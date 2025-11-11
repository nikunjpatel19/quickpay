package backend.models
// or: package com.quickpay.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkReq(
    val amountCents: Long,
    val currency: String,
    val description: String? = null
)

@Serializable
data class PaymentLinkDto(
    val id: String,
    val amountCents: Long,
    val currency: String,
    val description: String?,
    val checkoutUrl: String?,   // null until Finix creates it
    val status: String          // PENDING | PAID | CANCELED | EXPIRED
)

@Serializable
data class OrderDto(
    val id: String,
    val linkId: String,
    val status: String,         // CREATED | AUTHORIZED | CAPTURED | FAILED
    val amountCents: Long,
    val currency: String
)

@Serializable
data class ErrorRes(val message: String)