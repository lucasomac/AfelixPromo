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
     * @return Stores and offers in their configured display order.
     * @throws CatalogFormatException when a document violates the catalog schema.
     */
    fun map(documents: List<Pair<String, FirestoreStoreDocument>>): List<Store> =
        documents.map { [documentId, document] ->
            val storeName = document.name.trim()
            if (storeName.isBlank()) {
                throw CatalogFormatException("A loja '$documentId' não possui nome.")
            }
            if (document.sortOrder < 0) {
                throw CatalogFormatException("A loja '$documentId' possui uma ordem inválida.")
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
                sortOrder = document.sortOrder,
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
            .sortedWith(compareBy<OrderedStore> { it.sortOrder }.thenBy { it.documentId })
            .map(OrderedStore::store)

    private data class OrderedStore(
        val documentId: String,
        val sortOrder: Int,
        val store: Store,
    )
}

/**
 * Firestore representation of one store document.
 *
 * @property name Store display name.
 * @property iconUrl Optional HTTP(S) store logo.
 * @property sortOrder Stable position in the catalog.
 * @property offers Embedded offers belonging to this store.
 */
internal data class FirestoreStoreDocument(
    val name: String,
    val iconUrl: String? = null,
    val sortOrder: Int,
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
 * @property sortOrder Stable position among the store's offers.
 */
internal data class FirestoreOfferDocument(
    val id: String,
    val couponCode: String? = null,
    val affiliateUrl: String,
    val discountValue: String,
    val discountType: String,
    val sortOrder: Int,
)
