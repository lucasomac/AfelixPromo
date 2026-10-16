package br.com.lucolimac.afelixpromo.data.catalog

/**
 * Indicates that catalog source data does not match the supported JSON format.
 *
 * @param message Details of the invalid JSON content.
 */
class CatalogFormatException(message: String) : IllegalArgumentException(message)
