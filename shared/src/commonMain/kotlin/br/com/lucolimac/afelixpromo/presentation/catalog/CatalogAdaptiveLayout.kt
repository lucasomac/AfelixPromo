package br.com.lucolimac.afelixpromo.presentation.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import afelixpromo.shared.generated.resources.Res
import afelixpromo.shared.generated.resources.catalog_load_error_message
import afelixpromo.shared.generated.resources.catalog_load_error_title
import afelixpromo.shared.generated.resources.catalog_loading_title
import afelixpromo.shared.generated.resources.loading_please_wait
import afelixpromo.shared.generated.resources.retry
import afelixpromo.shared.generated.resources.select_store_message
import afelixpromo.shared.generated.resources.select_store_title
import br.com.lucolimac.afelixpromo.domain.model.Offer
import br.com.lucolimac.afelixpromo.domain.model.Store
import org.jetbrains.compose.resources.stringResource

/**
 * Adapts catalog and detail content between compact navigation and a persistent two-pane layout.
 *
 * @param loadState Current catalog loading state.
 * @param selectedStore Store selected for the detail pane, if any.
 * @param stores Stores matching the active search.
 * @param hasStores Whether the loaded catalog contains any stores.
 * @param isRefreshing Whether a user-initiated catalog refresh is in progress.
 * @param searchQuery Current search text.
 * @param paneSizing Two-pane allocation, or `null` for compact navigation.
 * @param onSearchQueryChange Called when the search text changes.
 * @param onStoreSelected Called when a store is selected from the catalog.
 * @param onCopyCoupon Called when an offer coupon should be copied.
 * @param onOpenStore Called when an offer destination should be opened.
 * @param onRetry Called when catalog loading should be retried.
 * @param onRefresh Called when the store list is pulled down to refresh.
 * @param modifier Modifier applied to the layout container.
 */
@Composable
internal fun CatalogAdaptiveLayout(
    loadState: CatalogLoadState,
    selectedStore: Store?,
    stores: List<Store>,
    hasStores: Boolean,
    isRefreshing: Boolean,
    searchQuery: String,
    displayMode: StoreDisplayMode,
    paneSizing: CatalogPaneSizing?,
    onSearchQueryChange: (String) -> Unit,
    onStoreSelected: (Store) -> Unit,
    onCopyCoupon: (Offer) -> Unit,
    onOpenStore: (Offer) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (paneSizing == null) {
        when (loadState) {
            CatalogLoadState.Loading -> EmptyCatalogMessage(
                title = stringResource(Res.string.catalog_loading_title),
                message = stringResource(Res.string.loading_please_wait),
                modifier = modifier,
            )
            CatalogLoadState.Failed -> EmptyCatalogMessage(
                title = stringResource(Res.string.catalog_load_error_title),
                message = stringResource(Res.string.catalog_load_error_message),
                action = onRetry,
                actionLabel = stringResource(Res.string.retry),
                modifier = modifier,
            )
            is CatalogLoadState.Loaded -> {
                if (selectedStore == null) {
                    StoreListScreen(
                        stores = stores,
                        hasStores = hasStores,
                        searchQuery = searchQuery,
                        isRefreshing = isRefreshing,
                        displayMode = displayMode,
                        onSearchQueryChange = onSearchQueryChange,
                        onStoreSelected = onStoreSelected,
                        onRefresh = onRefresh,
                        modifier = modifier,
                    )
                } else {
                    StoreOffersScreen(
                        store = selectedStore,
                        onCopyCoupon = onCopyCoupon,
                        onOpenStore = onOpenStore,
                        modifier = modifier,
                    )
                }
            }
        }
        return
    }

    when (loadState) {
        CatalogLoadState.Loading -> EmptyCatalogMessage(
            title = stringResource(Res.string.catalog_loading_title),
            message = stringResource(Res.string.loading_please_wait),
            modifier = modifier,
        )
        CatalogLoadState.Failed -> EmptyCatalogMessage(
            title = stringResource(Res.string.catalog_load_error_title),
            message = stringResource(Res.string.catalog_load_error_message),
            action = onRetry,
            actionLabel = stringResource(Res.string.retry),
            modifier = modifier,
        )
        is CatalogLoadState.Loaded -> Row(modifier = modifier) {
            StoreListScreen(
                stores = stores,
                hasStores = hasStores,
                searchQuery = searchQuery,
                isRefreshing = isRefreshing,
                displayMode = displayMode,
                onSearchQueryChange = onSearchQueryChange,
                onStoreSelected = onStoreSelected,
                onRefresh = onRefresh,
                modifier = Modifier.width(paneSizing.listWidth).fillMaxHeight(),
            )
            if (paneSizing.isHingeGap) {
                Spacer(Modifier.width(paneSizing.foldGap))
            } else {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
            Box(
                modifier = Modifier.width(paneSizing.detailWidth).fillMaxHeight(),
                contentAlignment = Alignment.TopStart,
            ) {
                if (selectedStore == null) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.select_store_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(Res.string.select_store_message),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    StoreOffersScreen(
                        store = selectedStore,
                        onCopyCoupon = onCopyCoupon,
                        onOpenStore = onOpenStore,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
