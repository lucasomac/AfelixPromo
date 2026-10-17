@file:OptIn(ExperimentalForeignApi::class)

package br.com.lucolimac.afelixpromo.data.service

import br.com.lucolimac.afelixpromo.data.catalog.CatalogFormatException
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreOfferDocument
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreStoreDocument
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSBundle
import platform.Foundation.NSNumber
import swiftPMImport.br.com.lucolimac.afelixpromo.shared.FIRApp
import swiftPMImport.br.com.lucolimac.afelixpromo.shared.FIRDocumentSnapshot
import swiftPMImport.br.com.lucolimac.afelixpromo.shared.FIRFirestore
import kotlin.collections.get
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Reads store records using the native Firebase Firestore SDK for Apple platforms.
 *
 * @return Store records from the server or the SDK's persistent local cache.
 * @throws IllegalStateException when Firebase is not configured or the query fails.
 * @throws CatalogFormatException when a returned document does not match the catalog schema.
 */
internal actual suspend fun loadFirestoreStoreDocuments(): List<Pair<String, FirestoreStoreDocument>> {
    if (FIRApp.defaultApp() == null) {
        if (NSBundle.mainBundle.pathForResource("GoogleService-Info", ofType = "plist") == null) {
            throw IllegalStateException("O arquivo GoogleService-Info.plist não está no bundle do app iOS.")
        }
        FIRApp.configure()
    }
    val query = FIRFirestore.firestore().collectionWithPath(STORES_COLLECTION)
    val snapshot = suspendCancellableCoroutine { continuation ->
        query.getDocumentsWithCompletion { result, error ->
            when {
                error != null -> continuation.resumeWithException(
                    IllegalStateException("A consulta Firestore falhou: ${error.localizedDescription}"),
                )
                result != null -> continuation.resume(result)
                else -> continuation.resumeWithException(
                    IllegalStateException("O Firestore retornou uma consulta sem resultado nem erro."),
                )
            }
        }
    }
    return snapshot.documents.map { rawDocument ->
        val document = rawDocument as? FIRDocumentSnapshot
            ?: throw CatalogFormatException("O Firestore retornou um documento incompatível.")
        val fields = document.data() as? Map<*, *>
            ?: throw CatalogFormatException("A loja '${document.documentID}' não contém dados.")
        document.documentID to FirestoreStoreDocument(
            name = fields.requiredString("name", document.documentID),
            iconUrl = fields.optionalString("iconUrl", document.documentID),
            offers = (fields["offers"] as? List<*>)?.mapIndexed { index, value ->
                val offer = value as? Map<*, *>
                    ?: throw CatalogFormatException(
                        "A oferta ${index + 1} da loja '${document.documentID}' não é um objeto.",
                    )
                val offerId = offer.requiredString("id", document.documentID)
                FirestoreOfferDocument(
                    id = offerId,
                    couponCode = offer.optionalString("couponCode", document.documentID),
                    affiliateUrl = offer.requiredString("affiliateUrl", document.documentID),
                    discountValue = offer.requiredString("discountValue", document.documentID),
                    discountType = offer.requiredString("discountType", document.documentID),
                    sortOrder = requiredSortOrder(
                        value = (offer["sortOrder"] as? NSNumber)?.doubleValue,
                        field = "sortOrder",
                        documentId = "${document.documentID}/$offerId",
                    ),
                )
            } ?: throw CatalogFormatException("A loja '${document.documentID}' não possui uma lista de ofertas."),
        )
    }
}
