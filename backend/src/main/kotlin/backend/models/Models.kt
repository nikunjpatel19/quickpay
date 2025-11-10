package com.quickpay.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateLinkReq(
    val amountCents: Int,
    val currency: String,
    val description: String
)

@Serializable
data class CreateLinkRes(
    val orderId: String,
    val url: String
)

/** status: pending | succeeded | failed */
@Serializable
data class Order(
    val id: String,
    val amountCents: Int,
    val currency: String,
    val description: String,
    val url: String,
    val status: String
)

@Serializable
data class ErrorRes(val message: String)
