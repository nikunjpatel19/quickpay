package backend.store

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone

object PaymentLinks : Table("payment_links") {
    val id = varchar("id", 64)
    val amountCents = long("amount_cents")
    val currency = varchar("currency", 3)
    val description = text("description").nullable()
    val checkoutUrl = text("checkout_url").nullable()
    val status = varchar("status", 32)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
    override val primaryKey = PrimaryKey(id)
}
