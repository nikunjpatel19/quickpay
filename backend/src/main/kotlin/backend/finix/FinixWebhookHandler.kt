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
        val link = event.embedded?.paymentLinks?.firstOrNull()
        if (link == null) {
            println("===> No payment_links in embedded")
            return
        }

        println("===> Payment Link id=${link.id}, state=${link.state}")

        // We store our internal order id in Finix tags
        val internalId = link.tags?.get("order_id")
        if (internalId == null) {
            println("===> No order_id tag on payment link, cannot map to local order")
            return
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

            "CANCELED" -> {
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
        // At the moment we just log transfers. If you want you can
        // also hook them into the DB later.
    }
}