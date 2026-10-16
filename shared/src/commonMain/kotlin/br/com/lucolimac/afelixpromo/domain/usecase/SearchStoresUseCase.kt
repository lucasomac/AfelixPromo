package br.com.lucolimac.afelixpromo.domain.usecase

import br.com.lucolimac.afelixpromo.domain.model.Store

/**
 * Filters stores by name, ignoring case and surrounding whitespace in the query.
 */
class SearchStoresUseCase {
    /**
     * Applies a name search to the supplied catalog.
     *
     * @param stores Complete catalog to search.
     * @param query User-entered store name fragment.
     * @return Matching stores, or the original list when the query is blank.
     */
    operator fun invoke(stores: List<Store>, query: String): List<Store> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return stores
        return stores.filter { it.name.contains(normalizedQuery, ignoreCase = true) }
    }
}
