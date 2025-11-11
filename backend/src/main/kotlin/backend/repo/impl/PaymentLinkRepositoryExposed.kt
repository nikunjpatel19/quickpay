package backend.repo.impl


import backend.models.CreateLinkReq
import backend.models.PaymentLinkDto
import backend.repo.PaymentLinkRepository
import backend.store.PaymentLinks
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class PaymentLinkRepositoryExposed : PaymentLinkRepository {
    override fun create(req: CreateLinkReq, generatedId: String, checkoutUrl: String?): PaymentLinkDto {
        transaction {
            PaymentLinks.insert {
                it[id] = generatedId
                it[amountCents] = req.amountCents
                it[currency] = req.currency
                it[description] = req.description
                it[status] = "PENDING"
                it[PaymentLinks.checkoutUrl] = checkoutUrl
            }
        }
        return get(generatedId)!!
    }

    override fun get(id: String): PaymentLinkDto? = transaction {
        PaymentLinks.selectAll().where { PaymentLinks.id eq id }
            .limit(1)
            .map {
                PaymentLinkDto(
                    id = it[PaymentLinks.id],
                    amountCents = it[PaymentLinks.amountCents],
                    currency = it[PaymentLinks.currency],
                    description = it[PaymentLinks.description],
                    checkoutUrl = it[PaymentLinks.checkoutUrl],
                    status = it[PaymentLinks.status]
                )
            }.singleOrNull()
    }

    override fun updateStatus(id: String, status: String): Boolean = transaction {
        PaymentLinks.update({ PaymentLinks.id eq id }) { it[PaymentLinks.status] = status } > 0
    }

    override fun setCheckoutUrl(id: String, url: String): Boolean = transaction {
        PaymentLinks.update({ PaymentLinks.id eq id }) { it[checkoutUrl] = url } > 0
    }
}
