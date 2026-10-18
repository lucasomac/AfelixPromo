package br.com.lucolimac.afelixpromo.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.lucolimac.afelixpromo.domain.model.Offer
import br.com.lucolimac.afelixpromo.domain.model.Store
import br.com.lucolimac.afelixpromo.domain.repository.CatalogRepository
import br.com.lucolimac.afelixpromo.domain.usecase.GetStoresUseCase
import br.com.lucolimac.afelixpromo.domain.usecase.SearchStoresUseCase
import br.com.lucolimac.afelixpromo.data.service.fetchStoreDisplayMode
import br.com.lucolimac.afelixpromo.domain.usecase.ValidateAffiliateUrlUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordinates catalog interactions and exposes UI state and one-shot effects.
 *
 * @property getStores Use case that loads the catalog.
 * @property searchStores Use case that filters stores by name.
 * @property validateAffiliateUrl Use case that validates affiliate destinations.
 */
class CatalogViewModel(
    /** Use case that loads the catalog. */
    private val getStores: GetStoresUseCase,
    /** Use case that filters stores by name. */
    private val searchStores: SearchStoresUseCase,
    /** Use case that validates affiliate destinations. */
    private val validateAffiliateUrl: ValidateAffiliateUrlUseCase,
) : ViewModel() {
    /** Observable state consumed by the Compose screen. */
    private val mutableUiState = MutableStateFlow(CatalogUiState())

    /** Read-only stream of the current screen state. */
    val uiState = mutableUiState.asStateFlow()

    /** Buffered one-shot effects consumed by the application UI. */
    private val effectChannel = Channel<CatalogUiEffect>(Channel.BUFFERED)

    /** Flow view of [effectChannel] for the UI layer. */
    val effects = effectChannel.receiveAsFlow()

    /** Complete catalog retained for local search. */
    private var stores: List<Store> = emptyList()

    /** Active catalog request, preventing concurrent retries and refresh gestures. */
    private var catalogLoadJob: Job? = null

    init {
        loadCatalog()
        loadDisplayMode()
    }

    private fun loadDisplayMode() {
        viewModelScope.launch {
            try {
                val mode = fetchStoreDisplayMode()
                mutableUiState.update { it.copy(displayMode = mode) }
            } catch (_: Exception) {
                // Keep default
            }
        }
    }

    /** Updates the visible catalog for a new search [query]. */
    fun onSearchQueryChange(query: String) {
        mutableUiState.update { state ->
            state.copy(
                searchQuery = query,
                visibleStores = searchStores(stores, query),
            )
        }
    }

    /** Selects [store] and displays its offers. */
    fun onStoreSelected(store: Store) {
        mutableUiState.update { it.copy(selectedStore = store) }
    }

    /** Clears the selected store and returns to the catalog screen. */
    fun onBackToCatalog() {
        mutableUiState.update { it.copy(selectedStore = null) }
    }

    /** Retries loading catalog data after a failure. */
    fun onRetry() {
        loadCatalog()
    }

    /** Refreshes the catalog after a downward swipe on the store list. */
    fun onRefresh() {
        loadCatalog(isRefresh = true)
    }

    /** Requests a clipboard action when [offer] has a non-blank coupon. */
    fun onCopyCoupon(offer: Offer) {
        offer.couponCode?.takeIf(String::isNotBlank)?.let { coupon ->
            emitEffect(CatalogUiEffect.CopyCoupon(coupon))
        }
    }

    /** Validates [offer]'s destination and requests that the platform open it. */
    fun onOpenAffiliateLink(offer: Offer) {
        if (validateAffiliateUrl(offer.affiliateUrl)) {
            emitEffect(CatalogUiEffect.OpenAffiliateLink(offer.affiliateUrl))
        } else {
            emitEffect(CatalogUiEffect.AffiliateLinkInvalid)
        }
    }

    /** Reports the result of the platform's attempt to open an affiliate link. */
    fun onAffiliateLinkOpenResult(opened: Boolean) {
        emitEffect(CatalogUiEffect.AffiliateLinkOpenResult(opened))
    }

    /** Loads the catalog and updates success, loading, or failure state. */
    private fun loadCatalog(isRefresh: Boolean = false) {
        if (catalogLoadJob?.isActive == true) return
        catalogLoadJob = viewModelScope.launch {
            mutableUiState.update { state ->
                state.copy(
                    loadState = if (isRefresh && state.loadState is CatalogLoadState.Loaded) {
                        state.loadState
                    } else {
                        CatalogLoadState.Loading
                    },
                    isRefreshing = isRefresh,
                )
            }
            try {
                stores = getStores()
                mutableUiState.update {
                    val refreshedSelectedStore = it.selectedStore?.let { selected ->
                        stores.firstOrNull { store -> store.name == selected.name }
                    }
                    it.copy(
                        loadState = CatalogLoadState.Loaded(stores),
                        isRefreshing = false,
                        selectedStore = refreshedSelectedStore,
                        visibleStores = searchStores(stores, it.searchQuery),
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                exception.printStackTrace()
                mutableUiState.update {
                    it.copy(
                        loadState = CatalogLoadState.Failed,
                        isRefreshing = false,
                    )
                }
            }
        }
    }

    /** Queues [effect] for one-time collection by the presentation layer. */
    private fun emitEffect(effect: CatalogUiEffect) {
        viewModelScope.launch {
            effectChannel.send(effect)
        }
    }
}

/**
 * Creates a catalog ViewModel with its domain use cases wired to [repository].
 *
 * @param repository Data source implementation for the catalog.
 * @return A ready-to-use catalog ViewModel.
 */
fun createCatalogViewModel(repository: CatalogRepository): CatalogViewModel =
    CatalogViewModel(
        getStores = GetStoresUseCase(repository),
        searchStores = SearchStoresUseCase(),
        validateAffiliateUrl = ValidateAffiliateUrlUseCase(),
    )
