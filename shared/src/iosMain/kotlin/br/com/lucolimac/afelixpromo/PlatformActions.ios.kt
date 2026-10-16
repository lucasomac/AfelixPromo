package br.com.lucolimac.afelixpromo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard

/**
 * Remembers iOS clipboard and URL actions backed by UIKit.
 */
@Composable
actual fun rememberPlatformActions(): PlatformActions = remember {
    object : PlatformActions {
        /** Writes plain text to the iOS general pasteboard. */
        override fun copyToClipboard(text: String) {
            UIPasteboard.generalPasteboard.string = text
        }

        /** Opens the URL through UIApplication and forwards its completion result. */
        override fun openUrl(url: String, onResult: (Boolean) -> Unit) {
            val destination = NSURL.URLWithString(url)
            val scheme = destination?.scheme?.lowercase()
            if (destination == null || scheme !in setOf("http", "https") || destination.host.isNullOrBlank()) {
                onResult(false)
                return
            }
            UIApplication.sharedApplication.openURL(
                destination,
                options = emptyMap<Any?, Any?>(),
                completionHandler = onResult,
            )
        }
    }
}
