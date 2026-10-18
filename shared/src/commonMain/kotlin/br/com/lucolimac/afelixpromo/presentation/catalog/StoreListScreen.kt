package br.com.lucolimac.afelixpromo.presentation.catalog

import afelixpromo.shared.generated.resources.Res
import afelixpromo.shared.generated.resources.affiliate_disclosure
import afelixpromo.shared.generated.resources.catalog_empty_message
import afelixpromo.shared.generated.resources.catalog_empty_title
import afelixpromo.shared.generated.resources.catalog_intro
import afelixpromo.shared.generated.resources.clear_search
import afelixpromo.shared.generated.resources.offer_count
import afelixpromo.shared.generated.resources.search_empty_message
import afelixpromo.shared.generated.resources.search_empty_title
import afelixpromo.shared.generated.resources.search_stores
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.lucolimac.afelixpromo.domain.model.Store
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Displays the searchable store catalog and affiliate disclosure.
 *
 * @param stores Stores matching the current search query.
 * @param hasStores Whether the complete loaded catalog contains any stores.
 * @param searchQuery Current search field text.
 * @param isRefreshing Whether a catalog refresh is in progress.
 * @param displayMode Display layout mode ([StoreDisplayMode]).
 * @param onSearchQueryChange Called when the user edits or clears the query.
 * @param onStoreSelected Called when a store is selected.
 * @param onRefresh Called when the user pulls down on the list.
 * @param modifier Modifier applied to the screen container.
 */
@Composable
internal fun StoreListScreen(
    stores: List<Store>,
    hasStores: Boolean,
    searchQuery: String,
    isRefreshing: Boolean,
    displayMode: StoreDisplayMode,
    onSearchQueryChange: (String) -> Unit,
    onStoreSelected: (Store) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.catalog_intro),
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.search_stores)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        TextButton(onClick = { onSearchQueryChange("") }) {
                            Text(stringResource(Res.string.clear_search))
                        }
                    }
                },
            )
            Text(
                text = stringResource(Res.string.affiliate_disclosure),
                style = MaterialTheme.typography.bodySmall,
            )
            when {
                !hasStores -> EmptyCatalogMessage(
                    title = stringResource(Res.string.catalog_empty_title),
                    message = stringResource(Res.string.catalog_empty_message),
                )

                stores.isEmpty() -> EmptyCatalogMessage(
                    title = stringResource(Res.string.search_empty_title),
                    message = stringResource(Res.string.search_empty_message),
                )

                displayMode == StoreDisplayMode.GRID -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(stores, key = { it.name }) { store ->
                        Card(
                            onClick = { onStoreSelected(store) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                StoreIcon(
                                    storeName = store.name,
                                    iconUrl = store.iconUrl,
                                )
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        store.name,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        text = pluralStringResource(
                                            Res.plurals.offer_count,
                                            store.offers.size,
                                            store.offers.size,
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(stores, key = { it.name }) { store ->
                        Card(
                            onClick = { onStoreSelected(store) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                StoreIcon(
                                    storeName = store.name,
                                    iconUrl = store.iconUrl,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(store.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = pluralStringResource(
                                            Res.plurals.offer_count,
                                            store.offers.size,
                                            store.offers.size,
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
