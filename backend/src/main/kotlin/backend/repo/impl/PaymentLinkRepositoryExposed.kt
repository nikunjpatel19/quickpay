package backend.repo.impl

import backend.models.CreateLinkReq
import backend.models.PaymentLinkDto
import backend.repo.PaymentLinkRepository
import backend.store.PaymentLinks
import kotlinx.datetime.Clock
import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlinx.datetime.toJavaInstant

private fun Instant.toOffsetDateTime(): OffsetDateTime =
    OffsetDateTime.ofInstant(this.toJavaInstant(), ZoneOffset.UTC)


class PaymentLinkRepositoryExposed : PaymentLinkRepository {

    override fun create(
        req: CreateLinkReq,
        generatedId: String,
        finixPaymentLinkId: String?, // NEW
        checkoutUrl: String?,
        createdAt: Instant,
        updatedAt: Instant
    ): PaymentLinkDto {

        transaction {
            PaymentLinks.insert {
                it[id] = generatedId
                it[amountCents] = req.amountCents
                it[currency] = req.currency
                it[description] = req.description
                it[PaymentLinks.checkoutUrl] = checkoutUrl
                it[PaymentLinks.finixPaymentLinkId] = finixPaymentLinkId // NEW
                it[status] = "pending"
                it[PaymentLinks.createdAt] = createdAt.toOffsetDateTime()
                it[PaymentLinks.updatedAt] = updatedAt.toOffsetDateTime()

            }
        }

        return get(generatedId)!!
    }

    override fun get(id: String): PaymentLinkDto? = transaction {
        PaymentLinks
            .selectAll()
            .where { PaymentLinks.id eq id }
            .limit(1)
            .map {
                PaymentLinkDto(
                    id = it[PaymentLinks.id],
                    finixPaymentLinkId = it[PaymentLinks.finixPaymentLinkId], // NEW
                    amountCents = it[PaymentLinks.amountCents],
                    currency = it[PaymentLinks.currency],
                    description = it[PaymentLinks.description],
                    checkoutUrl = it[PaymentLinks.checkoutUrl],
                    status = it[PaymentLinks.status]
                )
            }
            .singleOrNull()
    }


    override fun updateStatus(id: String, status: String): Boolean = transaction {
        PaymentLinks.update({ PaymentLinks.id eq id }) {
            it[PaymentLinks.status] = status
            it[PaymentLinks.updatedAt] = System.now().toOffsetDateTime()
        } > 0
    }

    override fun setCheckoutUrl(id: String, url: String): Boolean = transaction {
        PaymentLinks.update({ PaymentLinks.id eq id }) {
            it[PaymentLinks.checkoutUrl] = url
            it[PaymentLinks.updatedAt] = System.now().toOffsetDateTime()
        } > 0
    }

    override fun setFinixPaymentLinkId(id: String, finixId: String): Boolean = transaction {
        PaymentLinks.update({ PaymentLinks.id eq id }) {
            it[finixPaymentLinkId] = finixId
            it[updatedAt] = System.now().toOffsetDateTime()
        } > 0
    }
}