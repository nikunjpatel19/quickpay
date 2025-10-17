package com.quickpay.store

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect("jdbc:sqlite:quickpay.db", driver = "org.sqlite.JDBC")
        transaction { SchemaUtils.create(PaymentLinks, WebhookEvents) }
    }
}