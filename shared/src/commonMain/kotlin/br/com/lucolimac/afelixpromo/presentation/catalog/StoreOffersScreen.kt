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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import afelixpromo.shared.generated.resources.Res
import afelixpromo.shared.generated.resources.copy_coupon_and_follow_site
import afelixpromo.shared.generated.resources.coupon_label
import afelixpromo.shared.generated.resources.discount_label
import afelixpromo.shared.generated.resources.offers_available
import afelixpromo.shared.generated.resources.offers_empty_message
import afelixpromo.shared.generated.resources.offers_empty_title
import afelixpromo.shared.generated.resources.share_offer
import org.jetbrains.compose.resources.stringResource
import br.com.lucolimac.afelixpromo.domain.model.Offer
import br.com.lucolimac.afelixpromo.domain.model.Store
import br.com.lucolimac.afelixpromo.domain.model.discountLabel

/**
 * Displays the offers for a store with separate coupon-copy and store-share actions.
 *
 * @param store Store whose offers are displayed.
 * @param onCopyCoupon Called when the user requests copying an offer coupon.
 * @param onShareOffer Called when the user requests sharing an offer.
 * @param modifier Modifier applied to the screen container.
 */
@Composable
internal fun StoreOffersScreen(
    store: Store,
    onCopyCoupon: (Offer) -> Unit,
    onShareOffer: (Offer) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.offers_available),
            style = MaterialTheme.typography.titleMedium,
        )
        if (store.offers.isEmpty()) {
            EmptyCatalogMessage(
                title = stringResource(Res.string.offers_empty_title),
                message = stringResource(Res.string.offers_empty_message),
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
                        onShareOffer = onShareOffer,
                    )
                }
            }
        }
    }
}

/**
 * Displays one offer and exposes its available coupon and share actions.
 *
 * @param offer Offer to display.
 * @param onCopyCoupon Called when the user requests copying the coupon.
 * @param onShareOffer Called when the user requests sharing the offer.
 */
@Composable
private fun OfferCard(
    offer: Offer,
    onCopyCoupon: (Offer) -> Unit,
    onShareOffer: (Offer) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(Res.string.discount_label, offer.discountLabel()),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            offer.couponCode?.takeIf(String::isNotBlank)?.let { coupon ->
                Text(stringResource(Res.string.coupon_label, coupon))
                OutlinedButton(
                    onClick = { onCopyCoupon(offer) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.copy_coupon_and_follow_site))
                }
            }
            Button(
                onClick = { onShareOffer(offer) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.share_offer))
            }
            offer.description?.takeIf(String::isNotBlank)?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
