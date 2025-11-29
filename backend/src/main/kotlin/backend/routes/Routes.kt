package backend.routes

import backend.models.CreateLinkReq
import backend.models.CreateLinkRes
import backend.repo.OrderRepository
import backend.repo.PaymentLinkRepository
import backend.finix.FinixPaymentLinksClient
import backend.finix.FinixWebhookHandler
import backend.finix.FinixWebhookEvent
import backend.store.WebhookEvents

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.time.ZoneOffset

private val webhookJson = Json { ignoreUnknownKeys = true }

fun Application.registerRoutes(
    paymentLinks: PaymentLinkRepository,
    orders: OrderRepository
) = routing {
    route("/v1") {

        /**
         * POST /v1/links
         * Creates a Finix Payment Link + saves in DB + creates an Order for polling
         */
        post("/links") {
            val body = call.receive<CreateLinkReq>()

            // Generate our internal ID
            val internalId = UUID.randomUUID().toString().substring(0, 8)

            // 1. Call Finix to create payment link
            val finixRes = try {
                FinixPaymentLinksClient.createPaymentLink(
                    amountCents = body.amountCents,
                    currency = body.currency,
                    description = body.description,
                    tags = mapOf("order_id" to internalId)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return@post call.respond(
                    HttpStatusCode.BadGateway,
                    mapOf("error" to "Finix link creation failed")
                )
            }

            val now = Clock.System.now()

            // 2. Save payment link in DB
            paymentLinks.create(
                req = body,
                generatedId = internalId,
                checkoutUrl = finixRes.linkUrl,
                createdAt = now,
                updatedAt = now
            )

            // 3. Create order paired with this link
            orders.createForLink(
                orderId = internalId,
                linkId = internalId,
                amountCents = body.amountCents,
                currency = body.currency
            )

            // 4. Return proper model to Android
            val response = CreateLinkRes(
                orderId = internalId,
                url = finixRes.linkUrl,
                status = "pending"
            )

            call.respond(HttpStatusCode.Created, response)
        }

        /**
         * GET /v1/orders/{id}
         * Android polls this to check success/failure
         */
        get("/orders/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val order = orders.get(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(order)
        }

        /**
         * Dev endpoint to manually change order status for testing
         */
        post("/dev/orders/{id}/mark") {
            val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val status = call.request.queryParameters["status"] ?: "CAPTURED"

            val ok = orders.markDevStatus(id, status)
            if (ok) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }

        post("/finix/webhook") {

            val rawBody = call.receiveText()

            // Finix hits this with an empty body when it validates the URL
            if (rawBody.isBlank()) {
                println("===> Finix validation ping (empty body)")
                return@post call.respond(HttpStatusCode.OK)
            }

            val now = Clock.System.now()

            // Try to decode webhook
            val event = try {
                webhookJson.decodeFromString(FinixWebhookEvent.serializer(), rawBody)
            } catch (e: Exception) {
                e.printStackTrace()

                // Store invalid payload for debugging, but still return 200
                transaction {
                    WebhookEvents.insert {
                        it[eventType]  = "invalid"
                        it[payload]    = rawBody
                        it[receivedAt] = OffsetDateTime.ofInstant(now.toJavaInstant(), ZoneOffset.UTC)
                        it[processed]  = false
                    }
                }

                return@post call.respond(HttpStatusCode.OK)
            }

            // Store valid payload
            val rowId = transaction {
                WebhookEvents.insert {
                    it[eventType]  = "${event.entity}:${event.type}"
                    it[payload]    = rawBody
                    it[receivedAt] = OffsetDateTime.ofInstant(now.toJavaInstant(), ZoneOffset.UTC)
                    it[processed]  = false
                } get WebhookEvents.id
            }

            // Apply business logic
            FinixWebhookHandler.handle(event, orders, paymentLinks)

            // Mark as processed
            transaction {
                WebhookEvents.update({ WebhookEvents.id eq rowId }) {
                    it[processed] = true
                }
            }

            call.respond(HttpStatusCode.OK)
        }
    }
}