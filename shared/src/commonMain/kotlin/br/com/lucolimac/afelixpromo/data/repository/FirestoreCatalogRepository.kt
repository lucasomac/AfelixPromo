package br.com.lucolimac.afelixpromo.data.repository

import br.com.lucolimac.afelixpromo.data.catalog.FirestoreCatalogMapper
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreStoreDocument
import br.com.lucolimac.afelixpromo.domain.model.Store
import br.com.lucolimac.afelixpromo.domain.repository.CatalogRepository

/**
 * Loads the public store catalog from Cloud Firestore.
 *
 * @property firestoreProvider Supplies the configured Firestore client when a catalog load begins.
 * @property mapper Validates remote documents and converts them to domain models.
 */
class FirestoreCatalogRepository(
) : CatalogRepository {
    private val mapper = FirestoreCatalogMapper()

    /**
     * Reads the stores collection using Firestore's default cache/server behavior.
     *
     * @return Stores from the server or persistent Firestore cache, sorted by configured order.
     * @throws Exception when Firestore is unavailable, access is denied, or documents are invalid.
     */
    override suspend fun loadStores(): List<Store> {
        return mapper.map(loadFirestoreStoreDocuments())
    }
}

/**
 * Reads and decodes the stores collection through the current platform's native Firebase SDK.
 *
 * @return Stable document IDs paired with decoded Firestore store records.
 */
internal expect suspend fun loadFirestoreStoreDocuments(): List<Pair<String, FirestoreStoreDocument>>
