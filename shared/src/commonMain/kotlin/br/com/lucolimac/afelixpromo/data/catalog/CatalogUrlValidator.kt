package br.com.lucolimac.afelixpromo.data.catalog

import br.com.lucolimac.afelixpromo.domain.usecase.ValidateAffiliateUrlUseCase

/**
 * Normalizes and validates web destinations supplied by any catalog source.
 *
 * @property validateAffiliateUrl Domain rule for HTTP(S) URL validation.
 */
internal class CatalogUrlValidator(
    private val validateAffiliateUrl: ValidateAffiliateUrlUseCase = ValidateAffiliateUrlUseCase(),
) {
    /**
     * Adds HTTPS when [url] has no scheme and rejects invalid affiliate destinations.
     *
     * @param url Raw destination from the catalog source.
     * @param record Description used in validation errors.
     * @return A validated HTTP(S) destination.
     * @throws CatalogFormatException when the URL is invalid or uses a disallowed scheme.
     */
    fun normalizeAffiliateUrl(url: String, record: String): String =
        normalizeWebUrl(url, record, "link")

    /**
     * Normalizes an optional logo URL.
     *
     * @param url Raw logo URL from the catalog source.
     * @param record Description used in validation errors.
     * @return A validated HTTP(S) URL.
     * @throws CatalogFormatException when the URL is invalid or uses a disallowed scheme.
     */
    fun normalizeIconUrl(url: String, record: String): String =
        normalizeWebUrl(url, record, "ícone")

    private fun normalizeWebUrl(url: String, record: String, kind: String): String {
        val rawUrl = url.trim()
        val normalized = when {
            rawUrl.startsWith("http://", ignoreCase = true) ||
                rawUrl.startsWith("https://", ignoreCase = true) -> rawUrl
            "://" in rawUrl -> throw CatalogFormatException(
                "O $kind de $record usa um esquema não permitido.",
            )
            else -> "https://$rawUrl"
        }
        if (!validateAffiliateUrl(normalized)) {
            throw CatalogFormatException("O $kind de $record não é uma URL HTTP(S) válida.")
        }
        return normalized
    }
}
