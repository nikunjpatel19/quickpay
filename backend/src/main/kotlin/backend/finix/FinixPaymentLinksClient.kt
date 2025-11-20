package backend.finix

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class FinixAmountDetails(
    @SerialName("amount_type") val amountType: String = "FIXED",
    @SerialName("total_amount") val totalAmount: Long,
    val currency: String
)

@Serializable
data class FinixAdditionalDetails(
    @SerialName("terms_of_service_url") val termsOfServiceUrl: String
)

@Serializable
data class FinixPaymentLinkRequest(
    @SerialName("merchant_id") val merchantId: String,
    @SerialName("application_id") val applicationId: String,
    @SerialName("payment_frequency") val paymentFrequency: String = "ONE_TIME",
    @SerialName("is_multiple_use") val isMultipleUse: Boolean = false,
    @SerialName("allowed_payment_methods") val allowedPaymentMethods: List<String> =
        listOf("PAYMENT_CARD"),
    @SerialName("amount_details") val amountDetails: FinixAmountDetails,
    @SerialName("additional_details") val additionalDetails: FinixAdditionalDetails
)

@Serializable
data class FinixPaymentLinkResponse(
    val id: String,
    @SerialName("link_url") val linkUrl: String,
    val state: String,
    @SerialName("merchant_id") val merchantId: String,
    @SerialName("application_id") val applicationId: String
)

object FinixPaymentLinksClient {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    suspend fun createPaymentLink(
        amountCents: Long,
        currency: String,
        description: String?
    ): FinixPaymentLinkResponse {
        val cfg = FinixConfig

        val body = FinixPaymentLinkRequest(
            merchantId = cfg.merchantId,
            applicationId = cfg.applicationId,
            amountDetails = FinixAmountDetails(
                totalAmount = amountCents.toLong(),
                currency = currency
            ),
            additionalDetails = FinixAdditionalDetails(
                termsOfServiceUrl = cfg.termsOfServiceUrl
            )
        )

        return http.post("${cfg.baseUrl}/payment_links") {
            contentType(ContentType.Application.Json)
            header("Finix-Version", "2022-02-01")
            basicAuth(cfg.username, cfg.password)
            setBody(body)
        }.body()
    }
}
