package br.com.lucolimac.afelixpromo

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import kotlinx.coroutines.flow.collect

/**
 * Observes AndroidX WindowManager and reports separating fold bounds in Compose density-independent pixels.
 */
@Composable
actual fun rememberFoldingFeatureInfo(): FoldingFeatureInfo? {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val density = LocalDensity.current.density
    var foldingFeatureInfo by remember(activity) { mutableStateOf<FoldingFeatureInfo?>(null) }

    LaunchedEffect(activity, density) {
        if (activity == null) {
            foldingFeatureInfo = null
        } else {
            WindowInfoTracker.getOrCreate(activity)
                .windowLayoutInfo(activity)
                .collect { windowLayoutInfo ->
                    foldingFeatureInfo = windowLayoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull { it.isSeparating }
                        ?.let { feature ->
                            val orientation = when (feature.orientation) {
                                FoldingFeature.Orientation.VERTICAL -> FoldOrientation.Vertical
                                FoldingFeature.Orientation.HORIZONTAL -> FoldOrientation.Horizontal
                                else -> return@let null
                            }
                            FoldingFeatureInfo(
                                orientation = orientation,
                                leftDp = feature.bounds.left / density,
                                topDp = feature.bounds.top / density,
                                rightDp = feature.bounds.right / density,
                                bottomDp = feature.bounds.bottom / density,
                            )
                        }
                }
        }
    }

    return foldingFeatureInfo
}

/**
 * Finds the hosting activity through wrappers supplied by Compose and Android themes.
 *
 * @return The hosting activity, or `null` if this context is not attached to one.
 */
private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
