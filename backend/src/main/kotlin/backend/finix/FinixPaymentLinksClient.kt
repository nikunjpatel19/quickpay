package backend.finix

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.basicAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
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
        listOf("PAYMENT_CARD", "GOOGLE_PAY"),
    @SerialName("amount_details") val amountDetails: FinixAmountDetails,
    @SerialName("additional_details") val additionalDetails: FinixAdditionalDetails,
    @SerialName("branding") val branding: FinixBranding,    // <<-- REQUIRED
    @SerialName("tags") val tags: Map<String, String>? = null // ✅ Add tags support
)

@Serializable
data class FinixPaymentLinkResponse(
    val id: String,
    @SerialName("link_url") val linkUrl: String,
    val state: String
)

@Serializable
data class FinixBranding(
    @SerialName("brand_color") val brandColor: String,
    @SerialName("accent_color") val accentColor: String,
    val logo: String,
    val icon: String,
    @SerialName("button_font_color") val buttonFontColor: String
)

// NEW
@Serializable
data class FinixDeactivatePaymentLinkReq(
    val state: String = "DEACTIVATED"
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
        description: String?,
        tags: Map<String, String>? = null      // <<< add tags here
    ): FinixPaymentLinkResponse {

        val cfg = FinixConfig

        val body = FinixPaymentLinkRequest(
            merchantId = cfg.merchantId,
            applicationId = cfg.applicationId,
            amountDetails = FinixAmountDetails(
                totalAmount = amountCents,
                currency = currency
            ),
            additionalDetails = FinixAdditionalDetails(
                termsOfServiceUrl = cfg.termsOfServiceUrl
            ),
            branding = FinixBranding(
                brandColor = "#111823",
                accentColor = "#f3eeee",
                logo = "https://upload.wikimedia.org/wikipedia/commons/a/ab/Logo_TV_2015.png",
                icon = "https://upload.wikimedia.org/wikipedia/commons/a/ab/Logo_TV_2015.png",
                buttonFontColor = "#111823"
            ),
            tags = tags     // <<< assign tags
        )

        val httpResponse = http.post("${cfg.baseUrl}/payment_links") {
            contentType(ContentType.Application.Json)
            header("Finix-Version", "2022-02-01")
            basicAuth(cfg.username, cfg.password)
            setBody(body)
        }

        val raw = httpResponse.bodyAsText()
        println("FINIX RAW RESPONSE: $raw")

        return json.decodeFromString(FinixPaymentLinkResponse.serializer(), raw)
    }

    // NEW
    suspend fun deactivatePaymentLink(finixPaymentLinkId: String): FinixPaymentLinkResponse {
        val cfg = FinixConfig

        val httpResponse = http.put("${cfg.baseUrl}/payment_links/$finixPaymentLinkId") {
            contentType(ContentType.Application.Json)
            header("Finix-Version", "2022-02-01")
            basicAuth(cfg.username, cfg.password)
            setBody(FinixDeactivatePaymentLinkReq())
        }

        val raw = httpResponse.bodyAsText()
        println("FINIX RAW DEACTIVATE RESPONSE: $raw")

        return json.decodeFromString(FinixPaymentLinkResponse.serializer(), raw)
    }
}