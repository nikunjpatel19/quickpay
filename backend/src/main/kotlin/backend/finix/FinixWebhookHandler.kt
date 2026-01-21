package backend.finix

import backend.repo.OrderRepository
import backend.repo.PaymentLinkRepository

object FinixWebhookHandler {

    fun handle(
        event: FinixWebhookEvent,
        orders: OrderRepository,
        paymentLinks: PaymentLinkRepository
    ) {
        println("===> Finix event: entity=${event.entity}, type=${event.type}")

        when (event.entity) {
            "payment_link" -> handlePaymentLink(event, orders, paymentLinks)
            "transfer"     -> handleTransfer(event)
            else           -> println("===> Ignoring entity ${event.entity}")
        }
    }

    private fun handlePaymentLink(
        event: FinixWebhookEvent,
        orders: OrderRepository,
        paymentLinks: PaymentLinkRepository
    ) {
        val link = event.embedded?.paymentLinks?.firstOrNull() ?: run {
            println("===> No payment_links in embedded")
            return
        }

        val internalId = link.tags?.get("order_id") ?: run {
            println("===> No order_id tag on payment link, cannot map to local order")
            return
        }

        val local = paymentLinks.get(internalId)
        val localStatus = local?.status?.lowercase()

        // If we already cancelled locally, ignore late ACTIVE/COMPLETED.
        if (localStatus in setOf("cancelled", "canceled", "deactivated")) {
            val incoming = link.state.uppercase()
            if (incoming in setOf("ACTIVE", "COMPLETED")) {
                println("===> Ignoring late $incoming for locally cancelled order $internalId")
                return
            }
        }

        when (link.state.uppercase()) {
            "ACTIVE" -> {
                orders.markDevStatus(internalId, "CREATED")
                paymentLinks.updateStatus(internalId, "created")
            }

            "COMPLETED" -> {
                orders.markDevStatus(internalId, "CAPTURED")
                paymentLinks.updateStatus(internalId, "paid")
                println("===> Marked order $internalId as CAPTURED / paid")
            }

            "CANCELED", "CANCELLED", "DEACTIVATED" -> {
                orders.markDevStatus(internalId, "FAILED")
                paymentLinks.updateStatus(internalId, "cancelled")
            }

            else -> println("===> Unhandled payment_link state ${link.state}")
        }
    }

    private fun handleTransfer(event: FinixWebhookEvent) {
        val t = event.embedded?.transfers?.firstOrNull() ?: return
        println(
            "===> Transfer id=${t.id}, state=${t.state}, " +
                    "amount=${t.amount}, currency=${t.currency}"
        )
    }
}