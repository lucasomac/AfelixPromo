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

    /** The current JSON catalog parses every offer and retains descriptions and store icons. */
    @Test
    fun bundledJsonParsesEveryOfferAndRetainsDescriptions() {
        val json = File("src/commonMain/composeResources/files/catalog.json").readText(StandardCharsets.UTF_8)
        val stores = CatalogJsonParser().parse(json)

        assertEquals(10, stores.sumOf { it.offers.size })
        val magalu = stores.single { it.name == "Magalu" }
        assertEquals(10, magalu.offers.size)
        assertEquals(
            "Válido para compras à partir de R$ 499,90 vendidos e entregues pelo Magalu. Uso exclusivo na sua loja.",
            assertNotNull(magalu.offers.first().description),
        )
        assertEquals(
            "Valor",
            magalu.offers.first().discountType,
        )
        val magaluIconUrl = assertNotNull(magalu.iconUrl)
        assertTrue(magaluIconUrl.startsWith("https://"))
    }
}