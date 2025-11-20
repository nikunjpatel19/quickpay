package backend.repo

import backend.models.CreateLinkReq
import backend.models.PaymentLinkDto
import kotlinx.datetime.Instant

interface PaymentLinkRepository {
    fun create(
        req: CreateLinkReq,
        generatedId: String,
        checkoutUrl: String?,
        createdAt: Instant,
        updatedAt: Instant
    ): PaymentLinkDto

    fun get(id: String): PaymentLinkDto?
    fun updateStatus(id: String, status: String): Boolean
    fun setCheckoutUrl(id: String, url: String): Boolean
}