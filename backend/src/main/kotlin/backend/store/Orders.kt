package backend.store

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone

object Orders : Table("orders") {
    val id = varchar("id", 64)
    val linkId = varchar("link_id", 64).references(PaymentLinks.id)
    val status = varchar("status", 32)
    val amountCents = long("amount_cents")
    val currency = varchar("currency", 3)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
    override val primaryKey = PrimaryKey(id)
}
