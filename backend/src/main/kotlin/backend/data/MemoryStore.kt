package com.quickpay.backend.data

import com.quickpay.backend.models.Order
import java.util.concurrent.ConcurrentHashMap

object MemoryStore {
    private val orders = ConcurrentHashMap<String, Order>()

    fun save(order: Order) { orders[order.id] = order }
    fun find(orderId: String): Order? = orders[orderId]
    fun all(): List<Order> = orders.values.toList()

    fun updateStatus(orderId: String, newStatus: String): Boolean {
        val cur = orders[orderId] ?: return false
        orders[orderId] = cur.copy(status = newStatus)
        return true
    }
}
