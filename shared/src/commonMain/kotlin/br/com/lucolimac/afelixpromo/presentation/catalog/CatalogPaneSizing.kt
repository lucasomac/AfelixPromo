package br.com.lucolimac.afelixpromo.presentation.catalog

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.lucolimac.afelixpromo.FoldOrientation
import br.com.lucolimac.afelixpromo.FoldingFeatureInfo

/**
 * Width allocation for a persistent catalog list and store-detail pane.
 *
 * @property listWidth Width reserved for catalog search and results.
 * @property foldGap Width reserved for the fold or divider between panes.
 * @property detailWidth Width reserved for store offers.
 */
internal data class CatalogPaneSizing(
    val listWidth: Dp,
    val foldGap: Dp,
    val detailWidth: Dp,
    val isHingeGap: Boolean,
)

/**
 * Vertical placement and visible height for content kept within one side of a horizontal fold.
 *
 * @property topOffset Distance from the top of the app window to the selected pane.
 * @property height Height available inside that pane.
 */
internal data class FoldedWindowViewport(
    val topOffset: Dp,
    val height: Dp,
)

/**
 * Computes a two-pane layout only when both regions remain usable.
 *
 * @param availableWidth Current app window width.
 * @param foldingFeature Separating fold in window-relative density-independent pixels, if present.
 * @return Pane widths, or `null` when a compact single-screen layout is more appropriate.
 */
internal fun calculateCatalogPaneSizing(
    availableWidth: Dp,
    foldingFeature: FoldingFeatureInfo?,
): CatalogPaneSizing? {
    if (availableWidth < MIN_TWO_PANE_WIDTH || foldingFeature?.orientation == FoldOrientation.Horizontal) {
        return null
    }

    if (foldingFeature != null) {
        val listWidth = foldingFeature.leftDp.dp.coerceIn(0.dp, availableWidth)
        val foldGap = (foldingFeature.rightDp - foldingFeature.leftDp).dp.coerceAtLeast(0.dp)
        val detailWidth = (availableWidth - foldingFeature.rightDp.dp).coerceAtLeast(0.dp)
        return CatalogPaneSizing(listWidth, foldGap, detailWidth, isHingeGap = true)
            .takeIf { it.listWidth >= MIN_LIST_PANE_WIDTH && it.detailWidth >= MIN_DETAIL_PANE_WIDTH }
    }

    val dividerWidth = 1.dp
    val listWidth = (availableWidth * LIST_PANE_WIDTH_FRACTION)
        .coerceIn(MIN_LIST_PANE_WIDTH, availableWidth - MIN_DETAIL_PANE_WIDTH - dividerWidth)
    return CatalogPaneSizing(
        listWidth = listWidth,
        foldGap = 0.dp,
        detailWidth = availableWidth - listWidth - dividerWidth,
        isHingeGap = false,
    )
}

/**
 * Selects the larger usable region when a horizontal separating fold divides the window.
 *
 * @param availableHeight Current app window height.
 * @param foldingFeature Separating fold in window-relative density-independent pixels, if present.
 * @return The larger upper/lower region, or `null` when no horizontal separating fold exists.
 */
internal fun calculateFoldedWindowViewport(
    availableHeight: Dp,
    foldingFeature: FoldingFeatureInfo?,
): FoldedWindowViewport? {
    if (foldingFeature?.orientation != FoldOrientation.Horizontal) return null

    val upperHeight = foldingFeature.topDp.dp.coerceIn(0.dp, availableHeight)
    val lowerTop = foldingFeature.bottomDp.dp.coerceIn(upperHeight, availableHeight)
    val lowerHeight = availableHeight - lowerTop
    return if (upperHeight >= lowerHeight) {
        FoldedWindowViewport(topOffset = 0.dp, height = upperHeight)
    } else {
        FoldedWindowViewport(topOffset = lowerTop, height = lowerHeight)
    }.takeIf { it.height > 0.dp }
}

private val MIN_TWO_PANE_WIDTH = 640.dp
private val MIN_LIST_PANE_WIDTH = 280.dp
private val MIN_DETAIL_PANE_WIDTH = 320.dp
private const val LIST_PANE_WIDTH_FRACTION = 0.4f
