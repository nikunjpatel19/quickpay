package com.quickpay.webhook

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import java.util.Base64

suspend fun basicAuthGuard(call: ApplicationCall): Boolean {
    val hdr = call.request.headers["Authorization"] ?: return unauthorized(call)
    if (!hdr.startsWith("Basic ")) return unauthorized(call)
    val decoded = String(Base64.getDecoder().decode(hdr.removePrefix("Basic ").trim()))
    val parts = decoded.split(":", limit = 2)
    if (parts.size != 2) return unauthorized(call)
    val (u, p) = parts
    val ok = (u == System.getenv("WEBHOOK_BASIC_USER")) && (p == System.getenv("WEBHOOK_BASIC_PASS"))
    return if (ok) true else unauthorized(call)
}
private suspend fun unauthorized(call: ApplicationCall): Boolean {
    call.respond(HttpStatusCode.Unauthorized); return false
}