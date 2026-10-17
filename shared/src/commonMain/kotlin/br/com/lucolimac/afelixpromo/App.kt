package br.com.lucolimac.afelixpromo

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.lucolimac.afelixpromo.data.repository.LocalCatalogRepository
import br.com.lucolimac.afelixpromo.presentation.catalog.CatalogLoadState
import br.com.lucolimac.afelixpromo.presentation.catalog.CatalogUiEffect
import br.com.lucolimac.afelixpromo.presentation.catalog.CatalogViewModel
import br.com.lucolimac.afelixpromo.presentation.catalog.EmptyCatalogMessage
import br.com.lucolimac.afelixpromo.presentation.catalog.StoreListScreen
import br.com.lucolimac.afelixpromo.presentation.catalog.StoreOffersScreen
import br.com.lucolimac.afelixpromo.presentation.catalog.createCatalogViewModel
import br.com.lucolimac.afelixpromo.presentation.theme.AfelixPromoTheme
import afelixpromo.shared.generated.resources.Res
import afelixpromo.shared.generated.resources.app_name
import afelixpromo.shared.generated.resources.affiliate_link_invalid
import afelixpromo.shared.generated.resources.affiliate_link_open_failed
import afelixpromo.shared.generated.resources.affiliate_link_opening
import afelixpromo.shared.generated.resources.catalog_load_error_message
import afelixpromo.shared.generated.resources.catalog_load_error_title
import afelixpromo.shared.generated.resources.catalog_loading_title
import afelixpromo.shared.generated.resources.coupon_copied
import afelixpromo.shared.generated.resources.loading_please_wait
import afelixpromo.shared.generated.resources.retry
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Root composable that connects the catalog ViewModel to the shared UI and platform actions.
 */
@Composable
@Preview
fun App() {
    val catalogViewModel: CatalogViewModel = viewModel(
        factory = viewModelFactory {
            initializer { createCatalogViewModel(LocalCatalogRepository()) }
        },
    )
    val uiState by catalogViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val platformActions = rememberPlatformActions()

    SystemBackHandler(
        enabled = uiState.selectedStore != null,
        onBack = catalogViewModel::onBackToCatalog,
    )

    LaunchedEffect(catalogViewModel) {
        catalogViewModel.effects.collect { effect ->
            when (effect) {
                is CatalogUiEffect.CopyCoupon -> {
                    platformActions.copyToClipboard(effect.coupon)
                    snackbarHostState.showSnackbar(getString(Res.string.coupon_copied))
                }
                is CatalogUiEffect.OpenAffiliateLink -> {
                    platformActions.openUrl(effect.url, catalogViewModel::onAffiliateLinkOpenResult)
                }
                is CatalogUiEffect.AffiliateLinkInvalid ->
                    snackbarHostState.showSnackbar(getString(Res.string.affiliate_link_invalid))
                is CatalogUiEffect.AffiliateLinkOpenResult -> {
                    val message = if (effect.opened) {
                        getString(Res.string.affiliate_link_opening)
                    } else {
                        getString(Res.string.affiliate_link_open_failed)
                    }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    AfelixPromoTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (uiState.selectedStore != null) {
                        BackNavigationButton(onClick = catalogViewModel::onBackToCatalog)
                    }
                    Text(
                        text = uiState.selectedStore?.name ?: stringResource(Res.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            },
        ) { contentPadding ->
            when (val state = uiState.loadState) {
                CatalogLoadState.Loading -> EmptyCatalogMessage(
                    title = stringResource(Res.string.catalog_loading_title),
                    message = stringResource(Res.string.loading_please_wait),
                    modifier = Modifier.padding(contentPadding),
                )
                is CatalogLoadState.Failed -> EmptyCatalogMessage(
                    title = stringResource(Res.string.catalog_load_error_title),
                    message = stringResource(Res.string.catalog_load_error_message),
                    action = catalogViewModel::onRetry,
                    actionLabel = stringResource(Res.string.retry),
                    modifier = Modifier.padding(contentPadding),
                )
                is CatalogLoadState.Loaded -> {
                    val store = uiState.selectedStore
                    if (store == null) {
                        StoreListScreen(
                            stores = uiState.visibleStores,
                            hasStores = state.stores.isNotEmpty(),
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = catalogViewModel::onSearchQueryChange,
                            onStoreSelected = catalogViewModel::onStoreSelected,
                            modifier = Modifier.fillMaxSize().padding(contentPadding),
                        )
                    } else {
                        StoreOffersScreen(
                            store = store,
                            onCopyCoupon = catalogViewModel::onCopyCoupon,
                            onOpenStore = catalogViewModel::onOpenAffiliateLink,
                            modifier = Modifier.fillMaxSize().padding(contentPadding),
                        )
                    }
                }
            }
        }
    }
}
