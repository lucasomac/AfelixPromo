package br.com.lucolimac.afelixpromo.data.service

import br.com.lucolimac.afelixpromo.data.catalog.FirestoreStoreDocument

/**
 * Reads and decodes the stores collection through the current platform's native Firebase SDK.
 *
 * @return Stable document IDs paired with decoded Firestore store records.
 * @throws Exception when Firestore cannot read the collection or a returned document is invalid.
 */
internal expect suspend fun loadFirestoreStoreDocuments(): List<Pair<String, FirestoreStoreDocument>>

/** Collection containing the catalog documents on every supported platform. */
internal const val STORES_COLLECTION = "stores"