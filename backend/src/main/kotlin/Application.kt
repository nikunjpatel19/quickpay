package com.quickpay

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import com.quickpay.finix.FinixClient
import com.quickpay.routes.registerRoutes
import com.quickpay.store.DatabaseFactory
import io.ktor.server.plugins.calllogging.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    install(DefaultHeaders)
    install(CORS) { anyHost() } // tighten for prod
    install(CallLogging)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(cause.message ?: "Server error")
            throw cause
        }
    }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; explicitNulls = false })
    }
    install(Koin) {
        modules(module { single { FinixClient() } })
    }

    DatabaseFactory.init()
    registerRoutes()
}
