package br.com.lucolimac.afelixpromo.presentation.catalog

import br.com.lucolimac.afelixpromo.domain.model.Store

/**
 * Represents the catalog loading phase shown by the catalog screen.
 */
sealed interface CatalogLoadState {
    /** Catalog data is currently being requested. */
    data object Loading : CatalogLoadState
    /** Catalog data could not be loaded; the screen offers a localized retry action. */
    data object Failed : CatalogLoadState
    /** Catalog data was loaded successfully. */
    data class Loaded(
        /** Stores and offers returned by the repository. */
        val stores: List<Store>,
    ) : CatalogLoadState
}

/**
 * Immutable state rendered by the catalog and store-detail screens.
 *
 * @property loadState Current catalog loading result.
 * @property isRefreshing Whether a user-initiated catalog refresh is in progress.
 * @property searchQuery Current store search text.
 * @property selectedStore Store being viewed, or `null` when the catalog is visible.
 * @property visibleStores Catalog stores matching [searchQuery].
 */
data class CatalogUiState(
    /** Current catalog loading result. */
    val loadState: CatalogLoadState = CatalogLoadState.Loading,
    /** Whether a user-initiated catalog refresh is in progress. */
    val isRefreshing: Boolean = false,
    /** Current store search text. */
    val searchQuery: String = "",
    /** Store being viewed, or `null` when the catalog is visible. */
    val selectedStore: Store? = null,
    /** Catalog stores matching [searchQuery]. */
    val visibleStores: List<Store> = emptyList(),
    /** Store layout display mode. */
    val displayMode: StoreDisplayMode = StoreDisplayMode.LIST,
)

/**
 * One-shot presentation effects that require an external UI or platform action.
 */
sealed interface CatalogUiEffect {
    /** Copies the supplied coupon to the system clipboard. */
    data class CopyCoupon(
        /** Exact coupon text to place on the clipboard. */
        val coupon: String,
    ) : CatalogUiEffect
    /** Opens a validated affiliate destination with the platform URL handler. */
    data class OpenAffiliateLink(
        /** HTTP(S) destination to open. */
        val url: String,
    ) : CatalogUiEffect
    /** Reports an affiliate link that failed validation before opening. */
    data object AffiliateLinkInvalid : CatalogUiEffect
    /** Reports the result of handing an affiliate link to the operating system. */
    data class AffiliateLinkOpenResult(
        /** Whether the platform accepted the request to open the destination. */
        val opened: Boolean,
    ) : CatalogUiEffect
}
