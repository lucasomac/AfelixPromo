package br.com.lucolimac.afelixpromo.domain.model

/**
 * Formats the discount for display without guessing currency for value discounts.
 *
 * Percentage discounts receive a percent sign; other types retain their source label.
 */
fun Offer.discountLabel(): String =
    if (discountType.equals("Porcentagem", ignoreCase = true)) {
        "$discountValue%"
    } else {
        "$discountValue · $discountType"
    }
