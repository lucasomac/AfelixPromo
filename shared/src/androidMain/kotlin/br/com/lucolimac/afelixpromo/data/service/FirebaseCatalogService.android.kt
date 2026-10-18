package br.com.lucolimac.afelixpromo.data.service

import br.com.lucolimac.afelixpromo.data.catalog.CatalogFormatException
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreOfferDocument
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreStoreDocument
import br.com.lucolimac.afelixpromo.data.service.STORES_COLLECTION
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.tasks.await
import kotlin.collections.get

/**
 * Reads store records using the native Android Firebase Firestore SDK.
 *
 * @return Store records from the server or the SDK's persistent local cache.
 * @throws Exception when Firestore cannot read the collection or a returned document is invalid.
 */
internal actual suspend fun loadFirestoreStoreDocuments(): List<Pair<String, FirestoreStoreDocument>> {
    FirebaseApp.getInstance()
    return FirebaseFirestore.getInstance()
        .collection(STORES_COLLECTION)
        .get()
        .await()
        .documents
        .map { document -> document.toCatalogRecord() }
}

/**
 * Decodes one native Firestore document into the shared catalog DTO.
 *
 * @return Stable document ID and store record.
 * @throws CatalogFormatException when a field has an unsupported type or is missing.
 */
private fun DocumentSnapshot.toCatalogRecord(): Pair<String, FirestoreStoreDocument> {
    val fields = data
        ?: throw CatalogFormatException("A loja '$id' não contém dados.")
    val offers = fields["offers"] as? List<*>
        ?: throw CatalogFormatException("A loja '$id' não possui uma lista válida de ofertas.")
    return id to FirestoreStoreDocument(
        name = fields.requiredString("name", id),
        iconUrl = fields.optionalString("iconUrl", id),
        offers = offers.mapIndexed { index, value ->
            val offer = value as? Map<*, *>
                ?: throw CatalogFormatException("A oferta ${index + 1} da loja '$id' não é um objeto.")
            val offerId = offer.requiredString("id", id)
            FirestoreOfferDocument(
                id = offerId,
                couponCode = offer.optionalString("couponCode", id),
                affiliateUrl = offer.requiredString("affiliateUrl", id),
                discountValue = offer.requiredString("discountValue", id),
                discountType = offer.requiredString("discountType", id),
                description = offer.optionalString("description", id),
                sortOrder = requiredSortOrder(
                    value = (offer["sortOrder"] as? Number)?.toDouble(),
                    field = "sortOrder",
                    documentId = "$id/$offerId",
                ),
            )
        },
    )
}
