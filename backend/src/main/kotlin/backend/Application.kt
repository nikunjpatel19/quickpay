package com.quickpay.backend

import com.quickpay.backend.routes.registerV1Routes
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureRouting()

    registerV1Routes()
}
