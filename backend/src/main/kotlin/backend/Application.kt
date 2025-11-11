package com.quickpay.backend

import backend.db.DatabaseFactory
import backend.repo.impl.OrderRepositoryExposed
import backend.repo.impl.PaymentLinkRepositoryExposed
import backend.routes.registerRoutes
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun main() {
    // Bind explicitly so Postman/Android can reach it
    embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0"   // important: binds to all interfaces (incl. 127.0.0.1)
    ) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = false
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            }
        )
    }

    val paymentLinks = PaymentLinkRepositoryExposed()
    val orders = OrderRepositoryExposed()

    registerRoutes(paymentLinks, orders)
}