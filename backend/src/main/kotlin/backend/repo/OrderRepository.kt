package backend.repo

import backend.models.OrderDto

interface OrderRepository {
    fun get(id: String): OrderDto?
    fun markDevStatus(id: String, status: String): Boolean
    fun createForLink(
        orderId: String,
        linkId: String,
        amountCents: Long,
        currency: String,
        note: String? = null
    ): OrderDto

    // New: list recent orders for history screen
    fun listRecent(limit: Int = 20): List<OrderDto>

    fun cancel(id: String): Boolean
}
