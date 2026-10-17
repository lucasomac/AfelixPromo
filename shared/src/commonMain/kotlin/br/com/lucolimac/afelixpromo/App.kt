package br.com.lucolimac.afelixpromo

import afelixpromo.shared.generated.resources.Res
import afelixpromo.shared.generated.resources.affiliate_link_invalid
import afelixpromo.shared.generated.resources.affiliate_link_open_failed
import afelixpromo.shared.generated.resources.affiliate_link_opening
import afelixpromo.shared.generated.resources.app_name
import afelixpromo.shared.generated.resources.coupon_copied
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import br.com.lucolimac.afelixpromo.data.repository.FirestoreCatalogRepository
import br.com.lucolimac.afelixpromo.presentation.catalog.CatalogAdaptiveLayout
import br.com.lucolimac.afelixpromo.presentation.catalog.CatalogLoadState
import br.com.lucolimac.afelixpromo.presentation.catalog.CatalogUiEffect
import br.com.lucolimac.afelixpromo.presentation.catalog.CatalogViewModel
import br.com.lucolimac.afelixpromo.presentation.catalog.calculateCatalogPaneSizing
import br.com.lucolimac.afelixpromo.presentation.catalog.calculateFoldedWindowViewport
import br.com.lucolimac.afelixpromo.presentation.catalog.createCatalogViewModel
import br.com.lucolimac.afelixpromo.presentation.theme.AfelixPromoTheme
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
            initializer { createCatalogViewModel(FirestoreCatalogRepository()) }
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val foldingFeature = rememberFoldingFeatureInfo()
            val paneSizing = calculateCatalogPaneSizing(maxWidth, foldingFeature)
            val foldedViewport = calculateFoldedWindowViewport(maxHeight, foldingFeature)

            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    modifier = foldedViewport?.let { viewport ->
                        Modifier
                            .fillMaxWidth()
                            .height(viewport.height)
                            .offset(y = viewport.topOffset)
                    } ?: Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(vertical = 12.dp)
                                .then(
                                    if (paneSizing == null) {
                                        Modifier.padding(horizontal = 16.dp)
                                    } else {
                                        Modifier
                                    },
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (paneSizing == null) {
                                if (uiState.selectedStore != null) {
                                    BackNavigationButton(onClick = catalogViewModel::onBackToCatalog)
                                }
                                Text(
                                    text = uiState.selectedStore?.name ?: stringResource(Res.string.app_name),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            } else {
                                Box(
                                    modifier = Modifier.width(paneSizing.listWidth).padding(start = 16.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.app_name),
                                        style = MaterialTheme.typography.headlineSmall,
                                    )
                                }
                                if (paneSizing.isHingeGap) {
                                    Spacer(Modifier.width(paneSizing.foldGap))
                                } else {
                                    VerticalDivider(modifier = Modifier.height(32.dp))
                                }
                                Row(
                                    modifier = Modifier.width(paneSizing.detailWidth).padding(start = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    uiState.selectedStore?.let { store ->
                                        BackNavigationButton(onClick = catalogViewModel::onBackToCatalog)
                                        Text(
                                            text = store.name,
                                            style = MaterialTheme.typography.headlineSmall,
                                        )
                                    }
                                }
                            }
                        }
                    },
                ) { contentPadding ->
                    val loadedState = uiState.loadState as? CatalogLoadState.Loaded
                    CatalogAdaptiveLayout(
                        loadState = uiState.loadState,
                        selectedStore = uiState.selectedStore,
                        stores = uiState.visibleStores,
                        hasStores = loadedState?.stores?.isNotEmpty() == true,
                        searchQuery = uiState.searchQuery,
                        paneSizing = paneSizing,
                        onSearchQueryChange = catalogViewModel::onSearchQueryChange,
                        onStoreSelected = catalogViewModel::onStoreSelected,
                        onCopyCoupon = catalogViewModel::onCopyCoupon,
                        onOpenStore = catalogViewModel::onOpenAffiliateLink,
                        onRetry = catalogViewModel::onRetry,
                        modifier = Modifier.fillMaxSize().padding(contentPadding),
                    )
                }
            }
        }
    }
}
