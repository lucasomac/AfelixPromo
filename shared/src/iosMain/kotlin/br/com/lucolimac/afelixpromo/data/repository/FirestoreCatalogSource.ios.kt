@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package br.com.lucolimac.afelixpromo.data.repository

import br.com.lucolimac.afelixpromo.data.catalog.CatalogFormatException
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreOfferDocument
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreStoreDocument
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSBundle
import platform.Foundation.NSNumber
import swiftPMImport.br.com.lucolimac.afelixpromo.shared.FIRApp
import swiftPMImport.br.com.lucolimac.afelixpromo.shared.FIRDocumentSnapshot
import swiftPMImport.br.com.lucolimac.afelixpromo.shared.FIRFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Reads store records using the native Firebase Firestore SDK for Apple platforms.
 *
 * @return Store records from the server or the SDK's persistent local cache.
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
            sortOrder = fields.requiredSortOrder("sortOrder", document.documentID),
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
                    sortOrder = offer.requiredSortOrder("sortOrder", "${document.documentID}/$offerId"),
                )
            } ?: throw CatalogFormatException("A loja '${document.documentID}' não possui uma lista de ofertas."),
        )
    }
}

private fun Map<*, *>.requiredString(field: String, documentId: String): String =
    this[field] as? String
        ?: throw CatalogFormatException("O campo '$field' do documento '$documentId' não é texto.")

private fun Map<*, *>.optionalString(field: String, documentId: String): String? {
    val value = this[field] ?: return null
    return value as? String
        ?: throw CatalogFormatException("O campo '$field' do documento '$documentId' não é texto.")
}

private fun Map<*, *>.requiredSortOrder(field: String, documentId: String): Int {
    val value = this[field] as? NSNumber
        ?: throw CatalogFormatException("O campo '$field' do documento '$documentId' não é numérico.")
    val intValue = value.intValue
    if (value.doubleValue != intValue.toDouble()) {
        throw CatalogFormatException("O campo '$field' do documento '$documentId' não é um inteiro.")
    }
    return intValue
}

private const val STORES_COLLECTION = "stores"
