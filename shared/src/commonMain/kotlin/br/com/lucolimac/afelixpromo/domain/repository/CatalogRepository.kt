package br.com.lucolimac.afelixpromo.domain.repository

import br.com.lucolimac.afelixpromo.domain.model.Store

/**
 * Contract for read-only access to the store and offer catalog.
 */
interface CatalogRepository {
    /**
     * Loads and groups all available offers by store.
     *
     * @return Stores with their available offers in the implementation's display order.
     * @throws Exception when the source cannot be read or contains invalid data.
     */
    suspend fun loadStores(): List<Store>
}
