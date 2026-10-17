package br.com.lucolimac.afelixpromo.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import afelixpromo.shared.generated.resources.Res
import afelixpromo.shared.generated.resources.store_icon_content_description
import afelixpromo.shared.generated.resources.unknown_store_initial
import coil3.compose.SubcomposeAsyncImage
import org.jetbrains.compose.resources.stringResource

/**
 * Shows a store's remote icon, falling back to a rounded initial badge.
 *
 * @param storeName Store name used for accessibility and the fallback initial.
 * @param iconUrl Optional HTTP(S) icon URL; null or failed images use the fallback.
 */
@Composable
internal fun StoreIcon(
    storeName: String,
    iconUrl: String?,
) {
    val iconModifier = Modifier
        .size(48.dp)
        .clip(CircleShape)
    val initial = storeName.trim().firstOrNull()?.uppercase() ?: stringResource(Res.string.unknown_store_initial)

    if (iconUrl.isNullOrBlank()) {
        StoreInitialPlaceholder(initial, iconModifier)
    } else {
        SubcomposeAsyncImage(
            model = iconUrl,
            contentDescription = stringResource(Res.string.store_icon_content_description, storeName),
            modifier = iconModifier,
            contentScale = ContentScale.Crop,
            loading = { StoreInitialPlaceholder(initial, Modifier.size(48.dp)) },
            error = { StoreInitialPlaceholder(initial, Modifier.size(48.dp)) },
        )
    }
}

/**
 * Draws the rounded initial badge used while a store icon is unavailable.
 *
 * @param initial Initial character to display.
 * @param modifier Modifier controlling the placeholder size and shape.
 */
@Composable
private fun StoreInitialPlaceholder(
    initial: String,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
