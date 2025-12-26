package backend.repo

import backend.models.OrderDto

interface OrderRepository {
    fun get(id: String): OrderDto?
    fun markDevStatus(id: String, status: String): Boolean
    fun createForLink(orderId: String, linkId: String, amountCents: Long, currency: String): OrderDto

    // New: list recent orders for history screen
    fun listRecent(limit: Int = 20): List<OrderDto>
}
