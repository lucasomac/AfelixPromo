package br.com.lucolimac.afelixpromo

import androidx.compose.ui.unit.dp
import br.com.lucolimac.afelixpromo.presentation.catalog.calculateCatalogPaneSizing
import br.com.lucolimac.afelixpromo.presentation.catalog.calculateFoldedWindowViewport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Verifies adaptive pane allocation for compact, expanded, and folded windows.
 */
class CatalogPaneSizingTest {
    /** Compact windows retain single-screen navigation. */
    @Test
    fun compactWidthDoesNotCreateTwoPanes() {
        assertNull(calculateCatalogPaneSizing(600.dp, null))
    }

    /** A wide window assigns enough width to both catalog and offer details. */
    @Test
    fun expandedWidthCreatesTwoUsablePanes() {
        val panes = calculateCatalogPaneSizing(840.dp, null)

        assertEquals(336.dp, panes?.listWidth)
        assertEquals(503.dp, panes?.detailWidth)
    }

    /** Foldable inner displays can use two panes below typical tablet widths. */
    @Test
    fun unfoldedFoldableWidthCreatesTwoUsablePanes() {
        val panes = calculateCatalogPaneSizing(673.dp, null)

        assertEquals(280.dp, panes?.listWidth)
        assertEquals(392.dp, panes?.detailWidth)
    }

    /** A vertical separating hinge is reserved between the two panes. */
    @Test
    fun verticalHingeIsKeptClearWhenBothSidesFit() {
        val feature = FoldingFeatureInfo(
            orientation = FoldOrientation.Vertical,
            leftDp = 390f,
            topDp = 0f,
            rightDp = 410f,
            bottomDp = 800f,
        )

        val panes = calculateCatalogPaneSizing(800.dp, feature)

        assertEquals(390.dp, panes?.listWidth)
        assertEquals(20.dp, panes?.foldGap)
        assertEquals(390.dp, panes?.detailWidth)
        assertEquals(true, panes?.isHingeGap)
    }

    /** A centered hinge on a foldable inner display still leaves both panes usable. */
    @Test
    fun verticalHingeFitsOnUnfoldedFoldableWidth() {
        val feature = FoldingFeatureInfo(
            orientation = FoldOrientation.Vertical,
            leftDp = 326f,
            topDp = 0f,
            rightDp = 346f,
            bottomDp = 800f,
        )

        val panes = calculateCatalogPaneSizing(673.dp, feature)

        assertEquals(326.dp, panes?.listWidth)
        assertEquals(20.dp, panes?.foldGap)
        assertEquals(327.dp, panes?.detailWidth)
    }

    /** A horizontal separating fold uses a single pane rather than a side-by-side layout. */
    @Test
    fun horizontalHingeDoesNotCreateTwoPanes() {
        val feature = FoldingFeatureInfo(
            orientation = FoldOrientation.Horizontal,
            leftDp = 0f,
            topDp = 390f,
            rightDp = 800f,
            bottomDp = 410f,
        )

        assertNull(calculateCatalogPaneSizing(800.dp, feature))
    }

    /** A horizontal hinge chooses the larger of its two visible regions. */
    @Test
    fun horizontalFoldUsesLargerViewportRegion() {
        val feature = FoldingFeatureInfo(
            orientation = FoldOrientation.Horizontal,
            leftDp = 0f,
            topDp = 300f,
            rightDp = 800f,
            bottomDp = 320f,
        )

        val viewport = calculateFoldedWindowViewport(900.dp, feature)

        assertEquals(320.dp, viewport?.topOffset)
        assertEquals(580.dp, viewport?.height)
    }
}
