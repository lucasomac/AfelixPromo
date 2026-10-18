package br.com.lucolimac.afelixpromo

import br.com.lucolimac.afelixpromo.data.catalog.CatalogJsonParser
import br.com.lucolimac.afelixpromo.data.catalog.CatalogFormatException
import br.com.lucolimac.afelixpromo.domain.model.Offer
import br.com.lucolimac.afelixpromo.domain.model.Store
import br.com.lucolimac.afelixpromo.domain.model.discountLabel
import br.com.lucolimac.afelixpromo.domain.usecase.SearchStoresUseCase
import br.com.lucolimac.afelixpromo.domain.usecase.ValidateAffiliateUrlUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Verifies the shared domain and JSON parsing rules.
 */
class SharedCommonTest {

    /** Store search ignores case and surrounding query whitespace. */
    @Test
    fun filterStoresMatchesNameIgnoringCaseAndWhitespace() {
        val stores = listOf(
            Store("Loja Azul", emptyList()),
            Store("Mercado Verde", emptyList()),
        )

        assertEquals(listOf(stores.first()), SearchStoresUseCase()(stores, "  AZUL "))
    }

    /** A blank query leaves the store list unchanged. */
    @Test
    fun filterStoresReturnsAllStoresForBlankQuery() {
        val stores = listOf(Store("Loja Azul", emptyList()))

        assertEquals(stores, SearchStoresUseCase()(stores, "  "))
    }

    /** Affiliate URL validation permits only valid HTTP(S) destinations. */
    @Test
    fun validatesOnlyHttpLinksWithHosts() {
        val validateUrl = ValidateAffiliateUrlUseCase()
        assertEquals(true, validateUrl("https://loja.example/oferta"))
        assertEquals(false, validateUrl("javascript:alert(1)"))
        assertEquals(false, validateUrl("https://"))
        assertEquals(false, validateUrl("https://?redirect=example.com"))
        assertEquals(false, validateUrl("https://shop.example:port"))
    }

    /** JSON parsing groups multiple offers by store and normalizes web URLs. */
    @Test
    fun parsesJsonAndGroupsOffersByStore() {
        val json = """
            [
              {"store":"Loja Azul","cupon":"AZUL10","url":"www.azul.example","discount":10,"type":"Porcentagem","icon":"https://azul.example/icon.png","description":"Oferta especial"},
              {"store":"Loja Azul","cupon":"AZUL15","url":"https://azul.example/oferta?a=1&b=2","discount":15,"type":"Porcentagem","icon":""}
            ]
        """.trimIndent()

        val stores = CatalogJsonParser().parse(json)

        assertEquals(1, stores.size)
        assertEquals("Loja Azul", stores.single().name)
        assertEquals(2, stores.single().offers.size)
        assertEquals("https://www.azul.example", stores.single().offers.first().affiliateUrl)
        assertEquals("https://azul.example/icon.png", stores.single().iconUrl)
        assertEquals("Oferta especial", stores.single().offers.first().description)
        assertEquals(null, stores.single().offers.last().description)
    }

    /** An explicitly null optional description is accepted as absent. */
    @Test
    fun parsesNullOfferDescriptionAsAbsent() {
        val json =
            """[{"store":"Loja Azul","url":"https://azul.example","discount":10,"type":"Porcentagem","description":null}]"""

        assertEquals(null, CatalogJsonParser().parse(json).single().offers.single().description)
    }

    /** Invalid JSON records fail explicitly instead of being silently skipped. */
    @Test
    fun rejectsJsonOffersWithMissingRequiredFields() {
        val json = """[{"store":"Loja Azul","cupon":"AZUL10","url":"https://azul.example","discount":10}]"""

        assertFailsWith<CatalogFormatException> { CatalogJsonParser().parse(json) }
    }

    /** Boolean discount values are rejected rather than displayed as offer amounts. */
    @Test
    fun rejectsBooleanDiscountValues() {
        val json =
            """[{"store":"Loja Azul","cupon":"AZUL10","url":"https://azul.example","discount":true,"type":"Porcentagem"}]"""

        assertFailsWith<CatalogFormatException> { CatalogJsonParser().parse(json) }
    }

    /** Value discounts use pt-BR currency formatting while invalid values retain their source text. */
    @Test
    fun formatsDiscountLabelsWithBrazilianCurrencyForNumericValues() {
        val percentage = Offer("offer-1", null, "https://loja.example", "10", "Porcentagem")
        val value = percentage.copy(discountValue = "1234.5", discountType = "Valor")
        val decimalComma = value.copy(discountValue = "10,5")
        val invalidValue = value.copy(discountValue = "a partir de 10")

        assertEquals("10%", percentage.discountLabel())
        assertEquals("R$ 1.234,50", value.discountLabel())
        assertEquals("R$ 10,50", decimalComma.discountLabel())
        assertEquals("a partir de 10", invalidValue.discountLabel())
    }
}