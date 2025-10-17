package com.quickpay.finix

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.Base64

class FinixClient {
    private val client = HttpClient(Apache) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val base = System.getenv("FINIX_API_BASE") ?: "https://api-sandbox.finix.io"
    private val finixVersion = System.getenv("FINIX_VERSION") ?: "2022-01-01"
    private val basic = "Basic " + Base64.getEncoder().encodeToString(
        "${System.getenv("FINIX_USERNAME")}:${System.getenv("FINIX_PASSWORD")}".toByteArray()
    )

    // Return raw JSON text for now (keep it simple)
    suspend fun createPayment(amountCents: Long, currency: String, ref: String): String {
        val response = client.post("$base/payments") {
            header("Authorization", basic)
            header("Finix-Version", finixVersion)
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "amount" to amountCents,
                    "currency" to currency,
                    "statement_descriptor" to "QuickPay $ref"
                    // TODO: add payment source/token per your chosen Finix flow
                )
            )
        }
        return response.bodyAsText()
    }
}