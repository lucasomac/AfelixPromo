package br.com.lucolimac.afelixpromo

import br.com.lucolimac.afelixpromo.data.catalog.CatalogFormatException
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreCatalogMapper
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreOfferDocument
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreStoreDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Verifies Firestore document validation and conversion to domain catalog models.
 */
class FirestoreCatalogMapperTest {
    private val mapper = FirestoreCatalogMapper()

    /** Maps stores and offers in configured order and normalizes links missing HTTPS. */
    @Test
    fun mapsStoresAndOffersInConfiguredOrder() {
        val stores = mapper.map(
            listOf(
                "later" to store(name = "Later", sortOrder = 2),
                "first" to store(
                    name = " First ",
                    sortOrder = 1,
                    iconUrl = "logo.example",
                    offers = listOf(
                        offer(id = "second", sortOrder = 2),
                        offer(id = "first", sortOrder = 1, affiliateUrl = "shop.example"),
                    ),
                ),
            ),
        )

        assertEquals(listOf("First", "Later"), stores.map { it.name })
        assertEquals("https://logo.example", stores.first().iconUrl)
        assertEquals(listOf("first", "second"), stores.first().offers.map { it.id })
        assertEquals("https://shop.example", stores.first().offers.first().affiliateUrl)
    }

    /** Empty optional image and coupon values are represented as absent domain values. */
    @Test
    fun mapsEmptyOptionalValuesToNull() {
        val stores = mapper.map(
            listOf(
                "store" to store(
                    name = "Store",
                    iconUrl = "",
                    offers = listOf(offer(id = "offer", couponCode = " ")),
                ),
            ),
        )

        assertNull(stores.single().iconUrl)
        assertNull(stores.single().offers.single().couponCode)
    }

    /** A malformed destination fails the complete catalog load rather than dropping an offer. */
    @Test
    fun rejectsInvalidAffiliateUrls() {
        assertFailsWith<CatalogFormatException> {
            mapper.map(
                listOf(
                    "store" to store(
                        name = "Store",
                        offers = listOf(offer(id = "bad", affiliateUrl = "javascript:alert(1)")),
                    ),
                ),
            )
        }
    }

    /** Reused offer identifiers are rejected to preserve unambiguous catalog identity. */
    @Test
    fun rejectsDuplicateOfferIds() {
        assertFailsWith<CatalogFormatException> {
            mapper.map(
                listOf(
                    "store" to store(
                        name = "Store",
                        offers = listOf(offer(id = "duplicate"), offer(id = "duplicate", sortOrder = 1)),
                    ),
                ),
            )
        }
    }

    private fun store(
        name: String,
        sortOrder: Int = 0,
        iconUrl: String? = null,
        offers: List<FirestoreOfferDocument> = emptyList(),
    ) = FirestoreStoreDocument(
        name = name,
        sortOrder = sortOrder,
        iconUrl = iconUrl,
        offers = offers,
    )

    private fun offer(
        id: String,
        sortOrder: Int = 0,
        affiliateUrl: String = "https://shop.example",
        couponCode: String? = "SAVE10",
    ) = FirestoreOfferDocument(
        id = id,
        couponCode = couponCode,
        affiliateUrl = affiliateUrl,
        discountValue = "10",
        discountType = "Porcentagem",
        sortOrder = sortOrder,
    )
}
