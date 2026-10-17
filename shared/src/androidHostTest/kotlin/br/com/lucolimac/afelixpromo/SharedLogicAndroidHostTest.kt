package br.com.lucolimac.afelixpromo

import br.com.lucolimac.afelixpromo.data.catalog.CatalogJsonParser
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verifies the bundled JSON catalog against the shared parser.
 */
class SharedLogicAndroidHostTest {

    /** The JSON catalog parses every offer, normalizes URLs, and retains store icons. */
    @Test
    fun bundledJsonParsesEveryOfferAndNormalizesMissingSchemes() {
        val json = File("src/commonMain/composeResources/files/catalog.json").readText(StandardCharsets.UTF_8)
        val stores = CatalogJsonParser().parse(json)

        assertEquals(19, stores.sumOf { it.offers.size })
        assertEquals(
            "https://www.alvapersonalcare.com.br",
            stores.single { it.name == "Alva" }.offers.single().affiliateUrl,
        )
        assertEquals(
            "Valor",
            stores.single { it.name == "Probiotica" }.offers.single().discountType,
        )
        val braeIconUrl = assertNotNull(stores.single { it.name == "Braé" }.iconUrl)
        assertTrue(braeIconUrl.startsWith("https://"))
    }
}