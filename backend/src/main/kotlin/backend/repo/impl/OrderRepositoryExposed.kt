package backend.repo.impl

import backend.models.OrderDto
import backend.repo.OrderRepository
import backend.store.Orders
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class OrderRepositoryExposed : OrderRepository {

    override fun get(id: String): OrderDto? = transaction {
        Orders
            .selectAll()
            .where { Orders.id eq id }
            .limit(1)
            .map {
                OrderDto(
                    id = it[Orders.id],
                    linkId = it[Orders.linkId],
                    status = it[Orders.status],
                    amountCents = it[Orders.amountCents],
                    currency = it[Orders.currency],
                    note = it[Orders.note]          // NEW
                )
            }
            .singleOrNull()
    }

    override fun markDevStatus(id: String, status: String): Boolean = transaction {
        Orders.update({ Orders.id eq id }) {
            it[Orders.status] = status.lowercase()
        } > 0
    }

    override fun createForLink(orderId: String, linkId: String, amountCents: Long, currency: String, note: String?): OrderDto =
        transaction {
            Orders.insert {
                it[Orders.id] = orderId
                it[Orders.linkId] = linkId
                it[Orders.status] = "created"
                it[Orders.amountCents] = amountCents
                it[Orders.currency] = currency
                it[Orders.note] = note?.takeIf { n -> n.isNotBlank() }   // NEW
            }
            get(orderId)!!
        }

    // New
    override fun listRecent(limit: Int): List<OrderDto> = transaction {
        Orders
            .selectAll()
            .orderBy(Orders.createdAt to SortOrder.DESC)  // better than id
            .limit(limit)
            .map {
                OrderDto(
                    id = it[Orders.id],
                    linkId = it[Orders.linkId],
                    status = it[Orders.status],
                    amountCents = it[Orders.amountCents],
                    currency = it[Orders.currency],
                    note = it[Orders.note]                // NEW
                )
            }
    }

    override fun cancel(id: String): Boolean = transaction {
        val row = Orders
            .selectAll()
            .where { Orders.id eq id }
            .limit(1)
            .singleOrNull()
            ?: return@transaction false

        val status = row[Orders.status].lowercase()

        when (status) {
            "captured" -> return@transaction false
            "failed", "canceled", "cancelled" -> return@transaction true
        }

        Orders.update({ Orders.id eq id }) {
            it[Orders.status] = "failed" // terminal for Android polling
        } > 0
    }
}