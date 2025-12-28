package com.quickpay.backend

import backend.db.DatabaseFactory
import backend.repo.impl.OrderRepositoryExposed
import backend.repo.impl.PaymentLinkRepositoryExposed
import backend.routes.registerRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

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