package backend.routes

import backend.models.CreateLinkReq
import backend.repo.OrderRepository
import backend.repo.PaymentLinkRepository

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Application.registerRoutes(
    paymentLinks: PaymentLinkRepository,
    orders: OrderRepository
) = routing {
    route("/v1") {
        // POST /v1/links
        post("/links") {
            val body = call.receive<CreateLinkReq>()
            val id = UUID.randomUUID().toString().substring(0, 8)

            // create link (checkoutUrl = null for now)
            paymentLinks.create(body, id, checkoutUrl = null)

            // create paired order so we have something to poll
            orders.createForLink(orderId = id, linkId = id, amountCents = body.amountCents, currency = body.currency)

            // return the link (unchanged)
            call.respond(HttpStatusCode.Created, paymentLinks.get(id)!!)
        }

        // GET /v1/orders/{id}
        get("/orders/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val order = orders.get(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(order)
        }

        // POST /v1/dev/orders/{id}/mark?status=CAPTURED
        post("/dev/orders/{id}/mark") {
            val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val status = call.request.queryParameters["status"] ?: "CAPTURED"
            val ok = orders.markDevStatus(id, status)
            if (ok) call.respond(HttpStatusCode.OK) else call.respond(HttpStatusCode.NotFound)
        }
    }
}
