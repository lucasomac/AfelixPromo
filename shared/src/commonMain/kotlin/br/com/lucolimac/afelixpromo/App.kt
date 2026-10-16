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
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                    snackbarHostState.showSnackbar("Cupom copiado")
                }
                is CatalogUiEffect.OpenAffiliateLink -> {
                    platformActions.openUrl(effect.url, catalogViewModel::onAffiliateLinkOpenResult)
                }
                is CatalogUiEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF39434E),
            secondary = Color(0xFF59646D),
            background = Color(0xFFFAFAF8),
            surface = Color(0xFFFFFFFF),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF202326),
        ),
    ) {
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
                        text = uiState.selectedStore?.name ?: "AfelixPromo",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            },
        ) { contentPadding ->
            when (val state = uiState.loadState) {
                CatalogLoadState.Loading -> EmptyCatalogMessage(
                    title = "Carregando catálogo",
                    message = "Só um instante.",
                    modifier = Modifier.padding(contentPadding),
                )
                is CatalogLoadState.Failed -> EmptyCatalogMessage(
                    title = "Não foi possível carregar o catálogo",
                    message = state.message,
                    action = catalogViewModel::onRetry,
                    actionLabel = "Tentar novamente",
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
