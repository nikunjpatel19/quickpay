import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)

    // Ktor 3.x uses the GradleUp Shadow plugin. Do NOT use johnrengelman shadow.
    id("com.gradleup.shadow") version "9.1.0"
}

group = "com.quickpay"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    // Ktor HTTP client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
//    implementation(libs.ktor.client.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)

    // Exposed + Postgres
    implementation("org.jetbrains.exposed:exposed-core:0.56.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.56.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.56.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.56.0")
    implementation("org.postgresql:postgresql:42.7.4")

    // HikariCP
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Flyway for migrations
//    implementation("org.flywaydb:flyway-core:10.17.0")
//    implementation("org.flywaydb:flyway-database-postgresql:10.17.0")
    implementation("org.flywaydb:flyway-core:9.22.3")


    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.13")

//    // Ktor HTTP client
//    implementation("io.ktor:ktor-client-core:2.3.7")
//    implementation("io.ktor:ktor-client-cio:2.3.7")
//
//    // JSON support
//    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
//    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
//
//    // Logging plugin
//    implementation("io.ktor:ktor-client-logging:2.3.7")

}
tasks.withType<ShadowJar> {
    mergeServiceFiles()
    archiveClassifier.set("all")
}