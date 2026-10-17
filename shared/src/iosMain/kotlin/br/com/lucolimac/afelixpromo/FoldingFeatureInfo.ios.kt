package br.com.lucolimac.afelixpromo

import androidx.compose.runtime.Composable

/**
 * Returns no fold information because the iOS application uses window-size adaptation.
 *
 * @return Always `null`; fold posture is handled by the Android window implementation.
 */
@Composable
actual fun rememberFoldingFeatureInfo(): FoldingFeatureInfo? = null
