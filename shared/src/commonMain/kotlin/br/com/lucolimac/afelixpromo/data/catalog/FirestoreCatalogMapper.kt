package br.com.lucolimac.afelixpromo.data.catalog

import br.com.lucolimac.afelixpromo.domain.model.Offer
import br.com.lucolimac.afelixpromo.domain.model.Store

/**
 * Converts Firestore store documents into validated domain models.
 *
 * @property urlValidator Validates web destinations from remote documents.
 */
internal class FirestoreCatalogMapper(
    private val urlValidator: CatalogUrlValidator = CatalogUrlValidator(),
) {
    /**
     * Maps and validates a complete snapshot of store documents.
     *
     * @param documents Pairs of stable Firestore document IDs and decoded store documents.
     * @return Stores ordered alphabetically by name and offers ordered by their `sortOrder`.
     * @throws CatalogFormatException when a document violates the catalog schema.
     */
    fun map(documents: List<Pair<String, FirestoreStoreDocument>>): List<Store> =
        documents.map { [documentId, document] ->
            val storeName = document.name.trim()
            if (storeName.isBlank()) {
                throw CatalogFormatException("A loja '$documentId' não possui nome.")
            }

            val iconUrl = document.iconUrl
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?.let { urlValidator.normalizeIconUrl(it, "loja '$documentId'") }
            val offers = document.offers
                .onEach { offer ->
                    if (offer.id.isBlank()) {
                        throw CatalogFormatException("Uma oferta da loja '$documentId' não possui ID.")
                    }
                    if (offer.sortOrder < 0) {
                        throw CatalogFormatException("A oferta '${offer.id}' possui uma ordem inválida.")
                    }
                    if (offer.discountValue.isBlank()) {
                        throw CatalogFormatException("A oferta '${offer.id}' não possui desconto.")
                    }
                    if (offer.discountType.isBlank()) {
                        throw CatalogFormatException("A oferta '${offer.id}' não possui tipo de desconto.")
                    }
                }

            if (offers.map { it.id }.distinct().size != offers.size) {
                throw CatalogFormatException("A loja '$documentId' possui IDs de ofertas duplicados.")
            }

            OrderedStore(
                documentId = documentId,
                alphabeticalName = storeName.alphabeticalSortKey(),
                store = Store(
                    name = storeName,
                    iconUrl = iconUrl,
                    offers = offers
                        .sortedWith(compareBy<FirestoreOfferDocument> { it.sortOrder }.thenBy { it.id })
                        .map { offer ->
                            Offer(
                                id = offer.id,
                                couponCode = offer.couponCode?.trim()?.takeIf(String::isNotEmpty),
                                affiliateUrl = urlValidator.normalizeAffiliateUrl(
                                    offer.affiliateUrl,
                                    "oferta '${offer.id}' da loja '$documentId'",
                                ),
                                discountValue = offer.discountValue.trim(),
                                discountType = offer.discountType.trim(),
                            )
                        },
                ),
            )
        }
            .sortedWith(
                compareBy<OrderedStore> { it.alphabeticalName }
                    .thenBy { it.store.name.lowercase() }
                    .thenBy { it.documentId },
            )
            .map(OrderedStore::store)

    private data class OrderedStore(
        /** Stable Firestore ID used to break ties between identically named stores. */
        val documentId: String,
        /** Locale-independent lowercase name with common Latin diacritics folded. */
        val alphabeticalName: String,
        /** Validated domain model emitted after sorting. */
        val store: Store,
    )
}

/**
 * Creates a locale-independent key for alphabetically sorting Portuguese store names.
 *
 * @return Lowercase name with common Latin diacritics represented by their base letters.
 */
private fun String.alphabeticalSortKey(): String = lowercase().map { character ->
    when (character) {
        'á', 'à', 'â', 'ã', 'ä', 'å' -> 'a'
        'ç' -> 'c'
        'é', 'è', 'ê', 'ë' -> 'e'
        'í', 'ì', 'î', 'ï' -> 'i'
        'ñ' -> 'n'
        'ó', 'ò', 'ô', 'õ', 'ö' -> 'o'
        'ú', 'ù', 'û', 'ü' -> 'u'
        'ý', 'ÿ' -> 'y'
        else -> character
    }
}.joinToString("")

/**
 * Firestore representation of one store document.
 *
 * @property name Store display name.
 * @property iconUrl Optional HTTP(S) store logo.
 * @property offers Embedded offers belonging to this store.
 */
internal data class FirestoreStoreDocument(
    val name: String,
    val iconUrl: String? = null,
    val offers: List<FirestoreOfferDocument> = emptyList(),
)

/**
 * Firestore representation of an offer embedded in its store document.
 *
 * @property id Stable offer identifier.
 * @property couponCode Optional coupon text.
 * @property affiliateUrl HTTP(S) destination for the offer.
 * @property discountValue Discount value stored as text to preserve source formatting.
 * @property discountType Meaning of the discount value, as supplied by its source.
 * @property sortOrder Non-negative position used to control display order within the store.
 */
internal data class FirestoreOfferDocument(
    val id: String,
    val couponCode: String? = null,
    val affiliateUrl: String,
    val discountValue: String,
    val discountType: String,
    val sortOrder: Int,
)
