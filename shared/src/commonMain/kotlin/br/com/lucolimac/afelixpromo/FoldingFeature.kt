package br.com.lucolimac.afelixpromo

import androidx.compose.runtime.Composable

/** Orientation of a separating hinge or fold in the app window. */
enum class FoldOrientation {
    /** The fold runs vertically, separating left and right regions. */
    Vertical,
    /** The fold runs horizontally, separating upper and lower regions. */
    Horizontal,
}

/**
 * Window-relative bounds for a separating fold, measured in density-independent pixels.
 *
 * @property orientation Direction in which the fold separates the window.
 * @property leftDp Left edge of the fold relative to the app window.
 * @property topDp Top edge of the fold relative to the app window.
 * @property rightDp Right edge of the fold relative to the app window.
 * @property bottomDp Bottom edge of the fold relative to the app window.
 */
data class FoldingFeatureInfo(
    val orientation: FoldOrientation,
    val leftDp: Float,
    val topDp: Float,
    val rightDp: Float,
    val bottomDp: Float,
)

/**
 * Observes the current platform fold feature, if the platform exposes one.
 *
 * @return Current separating fold bounds, or `null` when there is no separating fold.
 */
@Composable
expect fun rememberFoldingFeatureInfo(): FoldingFeatureInfo?
