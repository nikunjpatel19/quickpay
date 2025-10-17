package com.quickpay.store

import org.jetbrains.exposed.sql.Table

object PaymentLinks : Table("payment_links") {
    val id = varchar("id", 40)
    val finixPaymentId = varchar("finix_payment_id", 64).nullable()
    val amount = long("amount_cents")
    val currency = varchar("currency", 3)
    val status = varchar("status", 20) // CREATED, PENDING, PAID, FAILED
    val qrUrl = varchar("qr_url", 256).nullable()
    override val primaryKey = PrimaryKey(id)
}

object WebhookEvents : Table("webhook_events") {
    val eventId = varchar("event_id", 80)
    override val primaryKey = PrimaryKey(eventId)
}