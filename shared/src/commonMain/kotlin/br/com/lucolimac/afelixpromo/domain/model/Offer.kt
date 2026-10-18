package br.com.lucolimac.afelixpromo.domain.model

/**
 * Formats the discount using the catalog type and Brazilian currency conventions.
 *
 * Percentage discounts receive a percent sign. Numeric value discounts are displayed in BRL
 * with two decimal places and Brazilian separators; nonnumeric values remain unchanged.
 */
fun Offer.discountLabel(): String =
    when {
        discountType.equals("Porcentagem", ignoreCase = true) -> "$discountValue%"
        discountType.equals("Valor", ignoreCase = true) ->
            discountValue.formatBrazilianCurrency() ?: discountValue
        else -> "$discountValue · $discountType"
    }

/**
 * Converts a plain decimal value into a Brazilian-real amount without platform locale APIs.
 *
 * A single comma or period may be used as the decimal separator. Values with grouping
 * separators or other nonnumeric characters are rejected so the caller can preserve the source.
 *
 * @return The amount prefixed with `R$`, or `null` when the value is not a plain decimal.
 */
private fun String.formatBrazilianCurrency(): String? {
    val value = trim()
    val negative = value.startsWith('-')
    val unsignedValue = value.removePrefix("-").removePrefix("+")
    if (unsignedValue.isEmpty()) return null

    val separatorIndex = unsignedValue.indexOfAny(charArrayOf('.', ','))
    val integerPart = if (separatorIndex < 0) unsignedValue else unsignedValue.substring(0, separatorIndex)
    val fractionalPart = if (separatorIndex < 0) "" else unsignedValue.substring(separatorIndex + 1)
    if (
        integerPart.isEmpty() ||
        integerPart.any { it !in '0'..'9' } ||
        fractionalPart.any { it !in '0'..'9' }
    ) {
        return null
    }
    if (separatorIndex >= 0 && unsignedValue.indexOfAny(charArrayOf('.', ','), separatorIndex + 1) >= 0) {
        return null
    }

    val minorUnits = (integerPart + fractionalPart.padEnd(2, '0').take(2))
        .trimStart('0')
        .ifEmpty { "0" }
        .let { digits ->
            if (fractionalPart.getOrNull(2)?.let { it >= '5' } == true) digits.incrementDigits() else digits
        }
    val normalizedMinorUnits = minorUnits.padStart(3, '0')
    val wholeAmount = normalizedMinorUnits.dropLast(2)
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
    val cents = normalizedMinorUnits.takeLast(2)
    val sign = if (negative && minorUnits.any { it != '0' }) "-" else ""
    return "${sign}R$ $wholeAmount,$cents"
}

/**
 * Adds one to a nonempty string of decimal digits without imposing a numeric size limit.
 *
 * @return The incremented decimal digits.
 */
private fun String.incrementDigits(): String {
    val digits = toCharArray()
    for (index in digits.lastIndex downTo 0) {
        if (digits[index] < '9') {
            digits[index] = digits[index] + 1
            return digits.concatToString()
        }
        digits[index] = '0'
    }
    return "1${digits.concatToString()}"
}
