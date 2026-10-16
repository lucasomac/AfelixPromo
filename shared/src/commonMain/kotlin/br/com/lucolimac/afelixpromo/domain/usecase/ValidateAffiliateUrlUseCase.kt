package br.com.lucolimac.afelixpromo.domain.usecase

/**
 * Checks whether an affiliate destination is a well-formed HTTP(S) URL.
 */
class ValidateAffiliateUrlUseCase {
    /**
     * Validates a candidate URL and its optional port.
     *
     * @param url Candidate URL to validate.
     * @return `true` only when the URL has an HTTP(S) scheme and a non-empty host.
     */
    operator fun invoke(url: String): Boolean {
        val normalized = url.trim()
        if (normalized.any(Char::isWhitespace)) return false
        val hasHttpScheme = normalized.startsWith("https://", ignoreCase = true) ||
            normalized.startsWith("http://", ignoreCase = true)
        if (!hasHttpScheme) return false

        val authority = normalized.substringAfter("://").substringBeforeAny('/', '?', '#')
        if (authority.isBlank() || '@' in authority) return false
        val host = authority.substringBefore(':')
        if (host.isBlank()) return false
        if (':' !in authority) return true
        val port = authority.substringAfterLast(':').toIntOrNull()
        return port != null && port in 1..65535
    }

    /** Returns the part of this string before its earliest matching delimiter. */
    private fun String.substringBeforeAny(vararg delimiters: Char): String {
        val delimiterIndex = indexOfAny(delimiters)
        return if (delimiterIndex < 0) this else substring(0, delimiterIndex)
    }
}
