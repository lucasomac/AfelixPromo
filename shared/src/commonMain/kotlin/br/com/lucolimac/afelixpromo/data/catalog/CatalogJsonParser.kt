package br.com.lucolimac.afelixpromo.data.catalog

import br.com.lucolimac.afelixpromo.domain.model.Offer
import br.com.lucolimac.afelixpromo.domain.model.Store
import br.com.lucolimac.afelixpromo.domain.usecase.ValidateAffiliateUrlUseCase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/**
 * Parses the bundled JSON catalog and validates its required offer fields.
 *
 * @property validateAffiliateUrl Domain rule used to validate normalized link values.
 */
class CatalogJsonParser(
    private val validateAffiliateUrl: ValidateAffiliateUrlUseCase = ValidateAffiliateUrlUseCase(),
) {
    private val urlValidator = CatalogUrlValidator(validateAffiliateUrl)

    /** Strict decoder that rejects JSON fields not represented by the DTO. */
    private val json = Json {
        ignoreUnknownKeys = false
        isLenient = false
    }

    /**
     * Converts a JSON array of store offers into stores grouped by store name.
     *
     * @param content JSON catalog content.
     * @return Parsed stores in first-seen order with their offers and optional icons.
     * @throws CatalogFormatException when the JSON or any required offer field is invalid.
     */
    fun parse(content: String): List<Store> {
        val entries = try {
            json.decodeFromString<List<CatalogOfferDto>>(content)
        } catch (exception: SerializationException) {
            throw CatalogFormatException("O JSON do catálogo possui estrutura ou valores inválidos: ${exception.message}")
        }

        val offersByStore = linkedMapOf<String, MutableList<CatalogOfferDto>>()
        entries.forEachIndexed { index, entry ->
            val storeName = entry.store.trim()
            if (storeName.isBlank()) {
                throw CatalogFormatException("A oferta ${index + 1} não possui nome de loja.")
            }
            val discountValue = entry.discount.content
            if (discountValue.isBlank() || entry.discount.isString.not() && entry.discount.content in BOOLEAN_VALUES) {
                throw CatalogFormatException("O desconto da oferta ${index + 1} não é válido.")
            }
            if (entry.type.isBlank()) {
                throw CatalogFormatException("A oferta ${index + 1} não possui tipo de desconto.")
            }
            val affiliateUrl = urlValidator.normalizeAffiliateUrl(entry.url, "oferta ${index + 1}")
            val normalizedEntry = entry.copy(
                store = storeName,
                url = affiliateUrl,
                icon = entry.icon?.trim()?.takeIf(String::isNotEmpty)?.let {
                    urlValidator.normalizeIconUrl(it, "oferta ${index + 1}")
                },
            )
            offersByStore.getOrPut(storeName) { mutableListOf() } += normalizedEntry
        }

        return offersByStore.map { [storeName, storeEntries] ->
            Store(
                name = storeName,
                iconUrl = storeEntries.firstNotNullOfOrNull { it.icon },
                offers = storeEntries.mapIndexed { index, entry ->
                    Offer(
                        id = "${storeName.slug()}-$index",
                        couponCode = entry.coupon?.trim()?.takeIf(String::isNotEmpty),
                        affiliateUrl = entry.url,
                        discountValue = entry.discount.content,
                        discountType = entry.type.trim(),
                    )
                },
            )
        }
    }

    /**
     * Creates a URL-safe identifier fragment for an offer derived from its store name.
     *
     * @return Lowercase store name with non-alphanumeric runs replaced by hyphens.
     */
    private fun String.slug(): String =
        lowercase().map { character ->
            if (character.isLetterOrDigit()) character else '-'
        }.joinToString("").trim('-').replace(Regex("-+"), "-")

    /**
     * JSON representation of one offer row.
     *
     * @property store Partner store name.
     * @property coupon Coupon code, or null if the JSON field is omitted or null.
     * @property url Affiliate destination.
     * @property discount Discount value represented as a JSON number or string.
     * @property type Source discount type label.
     * @property icon Optional store icon URL.
     */
    @Serializable
    private data class CatalogOfferDto(
        /** Partner store name. */
        @SerialName("store") val store: String,
        /** Coupon code, or null if the JSON field is omitted or null. */
        @SerialName("cupon") val coupon: String? = null,
        /** Affiliate destination. */
        @SerialName("url") val url: String,
        /** Discount value represented as a JSON number or string. */
        @SerialName("discount") val discount: JsonPrimitive,
        /** Source discount type label. */
        @SerialName("type") val type: String,
        /** Optional store icon URL. */
        @SerialName("icon") val icon: String? = null,
    )

    private companion object {
        /** JSON boolean tokens that must not be accepted as numeric discount values. */
        val BOOLEAN_VALUES = setOf("true", "false")
    }
}
