package com.quickpay.backend.routes

import com.quickpay.backend.data.MemoryStore
import com.quickpay.backend.models.CreateLinkReq
import com.quickpay.backend.models.CreateLinkRes
import com.quickpay.backend.models.ErrorRes
import com.quickpay.backend.models.Order
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.registerV1Routes() {
    routing {
        route("/v1") {

            post("/links") {
                val req = try { call.receive<CreateLinkReq>() }
                catch (_: Throwable) {
                    call.respond(HttpStatusCode.BadRequest, ErrorRes("Invalid JSON body"))
                    return@post
                }

                if (req.amountCents <= 0 || req.currency.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorRes("amountCents > 0 and currency required"))
                    return@post
                }

                val orderId = UUID.randomUUID().toString().substring(0, 8)
                val url = "https://mockpay.example/$orderId"

                val order = Order(
                    id = orderId,
                    amountCents = req.amountCents,
                    currency = req.currency.uppercase(),
                    description = req.description,
                    url = url,
                    status = "pending"
                )
                MemoryStore.save(order)
                call.respond(CreateLinkRes(orderId = orderId, url = url))
            }

            get("/orders/{id}") {
                val id = call.parameters["id"].orEmpty()
                val order = MemoryStore.find(id)
                if (order == null) call.respond(HttpStatusCode.NotFound, ErrorRes("Order not found"))
                else call.respond(order)
            }

            post("/dev/orders/{id}/mark") {
                val id = call.parameters["id"].orEmpty()
                val status = call.request.queryParameters["status"].orEmpty()
                if (status !in listOf("pending", "succeeded", "failed")) {
                    call.respond(HttpStatusCode.BadRequest, ErrorRes("status must be pending|succeeded|failed"))
                    return@post
                }
                val ok = MemoryStore.updateStatus(id, status)
                if (!ok) call.respond(HttpStatusCode.NotFound, ErrorRes("Order not found"))
                else call.respond(HttpStatusCode.OK)
            }
        }
    }
}
