package br.com.lucolimac.afelixpromo.data.repository

import afelixpromo.shared.generated.resources.Res
import br.com.lucolimac.afelixpromo.data.catalog.CatalogJsonParser
import br.com.lucolimac.afelixpromo.domain.model.Store
import br.com.lucolimac.afelixpromo.domain.repository.CatalogRepository
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Reads the catalog JSON bundled as a Compose Multiplatform resource.
 *
 * @property parser Converts and validates the bundled JSON content.
 */
class LocalCatalogRepository(
    private val parser: CatalogJsonParser = CatalogJsonParser(),
) : CatalogRepository {
    /**
     * Reads and parses the bundled JSON catalog.
     *
     * @return Stores and their offers from the bundled JSON.
     * @throws Exception when the resource cannot be read or parsed.
     */
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadStores(): List<Store> {
        val json = Res.readBytes("files/catalog.json").decodeToString(throwOnInvalidSequence = true)
        return parser.parse(json)
    }
}
