package backend.store

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone

//object WebhookEvents : Table("webhook_events") {
//    val id = long("id").autoIncrement()
//    val eventType = varchar("event_type", 128)
//    val payload = text("payload") // <-- fixed
//    val receivedAt = timestampWithTimeZone("received_at")
//    val processed = bool("processed").default(false)
//    override val primaryKey = PrimaryKey(id)
//}

object WebhookEvents : Table("webhook_events") {
    val id = long("id").autoIncrement()

    val finixEventId = text("finix_event_id").nullable()
    val entity = text("entity").nullable()
    val eventType = varchar("event_type", 128)
    val resourceId = text("resource_id").nullable()

    val payload = text("payload")
    val receivedAt = timestampWithTimeZone("received_at")
    val processed = bool("processed").default(false)

    override val primaryKey = PrimaryKey(id)
}
