package backend.models

import kotlinx.serialization.Serializable

/**
 * Android → Backend: Create a payment link
 */
@Serializable
data class CreateLinkReq(
    val amountCents: Long,
    val currency: String,
    val description: String? = null,
    val note: String? = null              // NEW
)

/**
 * Stored in DB + returned when Android polls a link
 */
@Serializable
data class PaymentLinkDto(
    val id: String,
    val amountCents: Long,
    val currency: String,
    val description: String?,
    val checkoutUrl: String?,   // becomes non-null once Finix returns link_url
    val status: String          // pending | paid | canceled | expired
)

/**
 * Android polls /v1/orders/{id}
 */
@Serializable
data class OrderDto(
    val id: String,
    val linkId: String,
    val status: String,         // created | authorized | captured | failed
    val amountCents: Long,
    val currency: String,
    val note: String? = null              // NEW
)

/**
 * Generic error response
 */
@Serializable
data class ErrorRes(
    val message: String
)

/**
 * What /v1/links returns to Android
 */
@Serializable
data class CreateLinkRes(
    val orderId: String,
    val url: String,
    val status: String
)