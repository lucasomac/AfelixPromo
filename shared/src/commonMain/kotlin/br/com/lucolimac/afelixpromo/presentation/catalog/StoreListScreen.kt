package br.com.lucolimac.afelixpromo.presentation.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.lucolimac.afelixpromo.domain.model.Store

/**
 * Displays the searchable store catalog and affiliate disclosure.
 *
 * @param stores Stores matching the current search query.
 * @param hasStores Whether the complete loaded catalog contains any stores.
 * @param searchQuery Current search field text.
 * @param onSearchQueryChange Called when the user edits or clears the query.
 * @param onStoreSelected Called when a store is selected.
 * @param modifier Modifier applied to the screen container.
 */
@Composable
internal fun StoreListScreen(
    stores: List<Store>,
    hasStores: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onStoreSelected: (Store) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Encontre uma loja e descubra seus cupons.",
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar lojas") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    TextButton(onClick = { onSearchQueryChange("") }) {
                        Text("Limpar")
                    }
                }
            },
        )
        Text(
            text = "Alguns links podem ser de afiliados. Isso pode gerar uma comissão sem custo extra para você.",
            style = MaterialTheme.typography.bodySmall,
        )
        when {
            !hasStores -> EmptyCatalogMessage(
                title = "Estamos preparando o catálogo",
                message = "As lojas e ofertas estarão disponíveis aqui em breve.",
            )
            stores.isEmpty() -> EmptyCatalogMessage(
                title = "Nenhuma loja encontrada",
                message = "Tente outro nome ou limpe sua busca.",
            )
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
                                    text = "${store.offers.size} ofertas",
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
