plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.quickpay"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    // DI
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // Ktor server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.call.logging)

    // (NEW) Better error handling + request validation
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.request.validation)

    // Exposed ORM
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    // (optional) DAO helpers
    implementation(libs.exposed.dao)

    // DB driver — use SQLite for local portfolio (REPLACE H2)
    // implementation(libs.h2)                    // ← remove this line
    implementation(libs.sqlite.jdbc)

    // (NEW) Ktor HTTP client for Finix API calls
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.apache)
    implementation(libs.ktor.client.content.negotiation)

    // logging
    implementation(libs.logback.classic)

    // tests
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
