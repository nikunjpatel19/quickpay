package backend.repo

import backend.models.CreateLinkReq
import backend.models.PaymentLinkDto

interface PaymentLinkRepository {
    fun create(req: CreateLinkReq, generatedId: String, checkoutUrl: String? = null): PaymentLinkDto
    fun get(id: String): PaymentLinkDto?
    fun updateStatus(id: String, status: String): Boolean
    fun setCheckoutUrl(id: String, url: String): Boolean
}
