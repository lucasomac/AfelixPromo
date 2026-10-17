package br.com.lucolimac.afelixpromo.data.repository

import br.com.lucolimac.afelixpromo.data.catalog.FirestoreCatalogMapper
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreStoreDocument
import br.com.lucolimac.afelixpromo.data.service.loadFirestoreStoreDocuments
import br.com.lucolimac.afelixpromo.domain.model.Store
import br.com.lucolimac.afelixpromo.domain.repository.CatalogRepository

/**
 * Loads the public store catalog from Cloud Firestore.
 */
class FirestoreCatalogRepository : CatalogRepository {
    private val mapper = FirestoreCatalogMapper()

    /**
     * Reads the stores collection using Firestore's default cache/server behavior.
     *
     * @return Stores from the server or persistent cache, ordered alphabetically by name.
     * @throws Exception when Firestore is unavailable, access is denied, or documents are invalid.
     */
    override suspend fun loadStores(): List<Store> {
        return mapper.map(loadFirestoreStoreDocuments())
    }
}