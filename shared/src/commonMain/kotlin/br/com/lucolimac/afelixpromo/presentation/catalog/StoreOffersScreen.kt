package br.com.lucolimac.afelixpromo.presentation.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.lucolimac.afelixpromo.domain.model.Offer
import br.com.lucolimac.afelixpromo.domain.model.Store
import br.com.lucolimac.afelixpromo.domain.model.discountLabel

/**
 * Displays the offers for a store with separate coupon-copy and store-access actions.
 *
 * @param store Store whose offers are displayed.
 * @param onCopyCoupon Called when the user requests copying an offer coupon.
 * @param onOpenStore Called when the user requests opening an offer destination.
 * @param modifier Modifier applied to the screen container.
 */
@Composable
internal fun StoreOffersScreen(
    store: Store,
    onCopyCoupon: (Offer) -> Unit,
    onOpenStore: (Offer) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Ofertas disponíveis",
            style = MaterialTheme.typography.titleMedium,
        )
        if (store.offers.isEmpty()) {
            EmptyCatalogMessage(
                title = "Nenhuma oferta disponível",
                message = "Volte em breve para conferir as ofertas desta loja.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(store.offers, key = { it.id }) { offer ->
                    OfferCard(
                        offer = offer,
                        onCopyCoupon = onCopyCoupon,
                        onOpenStore = onOpenStore,
                    )
                }
            }
        }
    }
}

/**
 * Displays one offer and exposes its available coupon and affiliate-link actions.
 *
 * @param offer Offer to display.
 * @param onCopyCoupon Called when the user requests copying the coupon.
 * @param onOpenStore Called when the user requests opening the destination.
 */
@Composable
private fun OfferCard(
    offer: Offer,
    onCopyCoupon: (Offer) -> Unit,
    onOpenStore: (Offer) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Desconto: ${offer.discountLabel()}",
                style = MaterialTheme.typography.titleMedium,
            )
            offer.couponCode?.takeIf(String::isNotBlank)?.let { coupon ->
                Text("Cupom: $coupon")
                OutlinedButton(
                    onClick = { onCopyCoupon(offer) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Copiar cupom")
                }
            }
            Button(
                onClick = { onOpenStore(offer) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Acessar loja")
            }
            Text(
                text = "Este link pode ser de afiliado.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
