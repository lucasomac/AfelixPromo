package br.com.lucolimac.afelixpromo

import br.com.lucolimac.afelixpromo.data.catalog.CatalogFormatException
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreCatalogMapper
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreOfferDocument
import br.com.lucolimac.afelixpromo.data.catalog.FirestoreStoreDocument
import br.com.lucolimac.afelixpromo.data.service.requiredSortOrder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Verifies Firestore document validation and conversion to domain catalog models.
 */
class FirestoreCatalogMapperTest {
    private val mapper = FirestoreCatalogMapper()

    /** Orders stores alphabetically, preserves offer order, and normalizes links missing HTTPS. */
    @Test
    fun mapsStoresAlphabeticallyAndOffersInConfiguredOrder() {
        val stores = mapper.map(
            listOf(
                "zulu" to store(name = "Zulu"),
                "eclair" to store(name = "Éclair"),
                "bravo" to store(
                    name = " bravo ",
                    iconUrl = "logo.example",
                    offers = listOf(
                        offer(id = "second", sortOrder = 2),
                        offer(id = "first", sortOrder = 1, affiliateUrl = "shop.example"),
                    ),
                ),
                "agua" to store(name = "Água"),
            ),
        )

        assertEquals(listOf("Água", "bravo", "Éclair", "Zulu"), stores.map { it.name })
        assertEquals("https://logo.example", stores[1].iconUrl)
        assertEquals(listOf("first", "second"), stores[1].offers.map { it.id })
        assertEquals("https://shop.example", stores[1].offers.first().affiliateUrl)
    }

    /** Empty optional image and coupon values are represented as absent domain values. */
    @Test
    fun mapsEmptyOptionalValuesToNull() {
        val stores = mapper.map(
            listOf(
                "store" to store(
                    name = "Store",
                    iconUrl = "",
                    offers = listOf(offer(id = "offer", couponCode = " ", description = " Termos da oferta ")),
                ),
            ),
        )

        assertNull(stores.single().iconUrl)
        assertNull(stores.single().offers.single().couponCode)
        assertEquals("Termos da oferta", stores.single().offers.single().description)
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

    /** Shared field decoding rejects fractional and out-of-range offer positions. */
    @Test
    fun rejectsInvalidOfferSortOrderValues() {
        assertEquals(2, requiredSortOrder(2.0, "sortOrder", "store/offer"))
        assertFailsWith<CatalogFormatException> {
            requiredSortOrder(2.5, "sortOrder", "store/offer")
        }
        assertFailsWith<CatalogFormatException> {
            requiredSortOrder(Double.NaN, "sortOrder", "store/offer")
        }
        assertFailsWith<CatalogFormatException> {
            requiredSortOrder(Int.MAX_VALUE.toDouble() + 1, "sortOrder", "store/offer")
        }
    }

    /**
     * Builds a store document fixture without production-only ordering metadata.
     *
     * @param name Store name to be mapped.
     * @param iconUrl Optional logo URL.
     * @param offers Offers embedded in the fixture.
     * @return A Firestore store document.
     */
    private fun store(
        name: String,
        iconUrl: String? = null,
        offers: List<FirestoreOfferDocument> = emptyList(),
    ) = FirestoreStoreDocument(
        name = name,
        iconUrl = iconUrl,
        offers = offers,
    )

    /**
     * Builds a valid offer fixture with a configurable display position.
     *
     * @param id Stable offer identifier.
     * @param sortOrder Position relative to offers in the same store.
     * @param affiliateUrl Affiliate destination to validate.
     * @param couponCode Optional coupon text.
     * @return A Firestore offer document.
     */
    private fun offer(
        id: String,
        sortOrder: Int = 0,
        affiliateUrl: String = "https://shop.example",
        couponCode: String? = "SAVE10",
        description: String? = null,
    ) = FirestoreOfferDocument(
        id = id,
        couponCode = couponCode,
        affiliateUrl = affiliateUrl,
        discountValue = "10",
        discountType = "Porcentagem",
        sortOrder = sortOrder,
        description = description,
    )
}
