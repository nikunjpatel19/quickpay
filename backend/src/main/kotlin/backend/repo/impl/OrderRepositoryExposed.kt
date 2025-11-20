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
                    currency = it[Orders.currency]
                )
            }
            .singleOrNull()
    }

    override fun markDevStatus(id: String, status: String): Boolean = transaction {
        Orders.update({ Orders.id eq id }) {
            it[Orders.status] = status.lowercase()
        } > 0
    }

    override fun createForLink(orderId: String, linkId: String, amountCents: Long, currency: String): OrderDto =
        transaction {
            Orders.insert {
                it[Orders.id] = orderId
                it[Orders.linkId] = linkId
                it[Orders.status] = "created"
                it[Orders.amountCents] = amountCents
                it[Orders.currency] = currency
            }
            get(orderId)!!
        }
}