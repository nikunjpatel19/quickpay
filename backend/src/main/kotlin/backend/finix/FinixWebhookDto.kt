package backend.finix

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Top level event object Finix sends
@Serializable
data class FinixWebhookEvent(
    val id: String,
    val type: String,      // "created", "updated"
    val entity: String,    // "payment_link" or "transfer"
    @SerialName("_embedded")
    val embedded: FinixEmbedded? = null
)

@Serializable
data class FinixEmbedded(
    @SerialName("payment_links")
    val paymentLinks: List<FinixPaymentLink>? = null,

    @SerialName("transfers")
    val transfers: List<FinixTransfer>? = null
)

// We only care about id, state and tags
@Serializable
data class FinixPaymentLink(
    val id: String,
    val state: String,
    val tags: Map<String, String>? = null
)

// For now we just log transfers, but keep amount as Double to avoid
// the "22.533" parse error you saw earlier
@Serializable
data class FinixTransfer(
    val id: String,
    val amount: Double? = null,
    val currency: String? = null,
    val state: String
)