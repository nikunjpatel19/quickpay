package com.quickpay.app.presentation.ui

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

fun formatCents(amountCents: Long, currency: String): String {
    val dollars = BigDecimal(amountCents).movePointLeft(2).setScale(2, RoundingMode.HALF_UP)
    val cur = currency.uppercase(Locale.US)

    val symbol = when (cur) {
        "USD", "CAD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> ""
    }

    return if (symbol.isNotBlank()) "$symbol$dollars $cur" else "$dollars $cur"
}