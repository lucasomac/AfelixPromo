package br.com.lucolimac.afelixpromo.domain.usecase

import br.com.lucolimac.afelixpromo.domain.model.Store
import br.com.lucolimac.afelixpromo.domain.repository.CatalogRepository

/**
 * Loads the catalog through its domain repository contract.
 *
 * @property catalogRepository Source used to retrieve stores and offers.
 */
class GetStoresUseCase(
    private val catalogRepository: CatalogRepository,
) {
    /**
     * Loads the current catalog.
     *
     * @return Stores with their associated offers.
     */
    suspend operator fun invoke(): List<Store> = catalogRepository.loadStores()
}
