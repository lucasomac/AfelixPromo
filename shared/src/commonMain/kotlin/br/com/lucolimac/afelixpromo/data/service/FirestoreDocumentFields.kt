package br.com.lucolimac.afelixpromo.data.service

import br.com.lucolimac.afelixpromo.data.catalog.CatalogFormatException

/**
 * Reads a required text field from a decoded Firestore document.
 *
 * @param field Name of the field to read.
 * @param documentId Firestore document containing the field, used in validation errors.
 * @return The field value.
 * @throws CatalogFormatException when the field is absent or is not a string.
 */
internal fun Map<*, *>.requiredString(field: String, documentId: String): String =
    this[field] as? String
        ?: throw CatalogFormatException("O campo '$field' do documento '$documentId' não é texto.")

/**
 * Reads an optional text field from a decoded Firestore document.
 *
 * @param field Name of the field to read.
 * @param documentId Firestore document containing the field, used in validation errors.
 * @return The string value, or `null` when the field is absent or null.
 * @throws CatalogFormatException when a present field is not a string.
 */
internal fun Map<*, *>.optionalString(field: String, documentId: String): String? {
    val value = this[field] ?: return null
    return value as? String
        ?: throw CatalogFormatException("O campo '$field' do documento '$documentId' não é texto.")
}

/**
 * Validates a platform-decoded numeric field as an integer offer position.
 *
 * @param value Numeric value converted to a double by the platform adapter, or `null` if not numeric.
 * @param field Name of the field being validated.
 * @param documentId Firestore document containing the field, used in validation errors.
 * @return The integer value.
 * @throws CatalogFormatException when the field is not numeric, finite, integral, or in `Int` range.
 */
internal fun requiredSortOrder(value: Double?, field: String, documentId: String): Int {
    if (value == null || !value.isFinite()) {
        throw CatalogFormatException("O campo '$field' do documento '$documentId' não é numérico.")
    }
    if (value % 1.0 != 0.0 || value < Int.MIN_VALUE || value > Int.MAX_VALUE) {
        throw CatalogFormatException("O campo '$field' do documento '$documentId' não é um inteiro válido.")
    }
    return value.toInt()
}
