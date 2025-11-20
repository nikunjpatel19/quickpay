package backend.finix

object FinixConfig {
    private fun env(name: String): String =
        System.getenv(name) ?: error("Missing env var: $name")

    val baseUrl: String = System.getenv("FINIX_BASE_URL") ?: "https://finix.sandbox-payments-api.com"
    val username: String = env("FINIX_USERNAME")
    val password: String = env("FINIX_PASSWORD")
    val merchantId: String = env("FINIX_MERCHANT_ID")
    val applicationId: String = env("FINIX_APPLICATION_ID")

    // simple demo term URL, you can hardcode or put in env
    val termsOfServiceUrl: String =
        System.getenv("FINIX_TERMS_URL") ?: "https://quickpay-demo.example.com/terms"
}
