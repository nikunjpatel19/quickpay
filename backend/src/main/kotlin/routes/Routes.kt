package com.quickpay.routes

import com.quickpay.models.CreateLinkReq
import com.quickpay.models.PaymentLinkDto
import com.quickpay.store.PaymentLinks
import com.quickpay.store.WebhookEvents
import com.quickpay.webhook.basicAuthGuard
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun Application.registerRoutes() = routing {
    route("/api") {
        post("/payment-links") {
            val body = call.receive<CreateLinkReq>()
            val id = UUID.randomUUID().toString().substring(0, 8)
            transaction {
                PaymentLinks.insert {
                    it[PaymentLinks.id] = id
                    it[amount] = body.amountCents
                    it[currency] = body.currency
                    it[status] = "CREATED"
                    it[qrUrl] = "${System.getenv("PUBLIC_BASE_URL") ?: "http://localhost:8080"}/checkout/$id"
                }
            }
            val dto = transaction {
                PaymentLinks.select { PaymentLinks.id eq id }.single().let {
                    PaymentLinkDto(
                        id = it[PaymentLinks.id],
                        amountCents = it[PaymentLinks.amount],
                        currency = it[PaymentLinks.currency],
                        status = it[PaymentLinks.status],
                        qr = it[PaymentLinks.qrUrl]!!
                    )
                }
            }
            call.respond(HttpStatusCode.Created, dto)
        }

        get("/payment-links") {
            val list = transaction {
                PaymentLinks.selectAll().orderBy(PaymentLinks.id to SortOrder.DESC).map {
                    PaymentLinkDto(
                        id = it[PaymentLinks.id],
                        amountCents = it[PaymentLinks.amount],
                        currency = it[PaymentLinks.currency],
                        status = it[PaymentLinks.status],
                        qr = it[PaymentLinks.qrUrl]!!
                    )
                }
            }
            call.respond(list)
        }

        get("/status/{id}") {
            val id = call.parameters["id"]!!
            val status = transaction {
                PaymentLinks.slice(PaymentLinks.status)
                    .select { PaymentLinks.id eq id }
                    .singleOrNull()?.get(PaymentLinks.status) ?: "NOT_FOUND"
            }
            call.respond(mapOf("id" to id, "status" to status))
        }

        post("/webhooks/finix") {
            if (!basicAuthGuard(call)) return@post
            val payload = call.receive<Map<String, Any?>>()
            val eventId = payload["id"]?.toString()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "missing id")

            val exists = transaction { WebhookEvents.select { WebhookEvents.eventId eq eventId }.any() }
            if (!exists) {
                transaction { WebhookEvents.insert { it[WebhookEvents.eventId] = eventId } }
                val type = payload["type"]?.toString()?.lowercase().orEmpty()
                val finixPaymentId = (payload["data"] as? Map<*, *>)?.get("id")?.toString()
                if ("payment.succeeded" in type && finixPaymentId != null) {
                    transaction {
                        PaymentLinks.update({ PaymentLinks.finixPaymentId eq finixPaymentId }) { it[status] = "PAID" }
                    }
                }
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}