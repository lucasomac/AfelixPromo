package br.com.lucolimac.afelixpromo

import androidx.compose.runtime.Composable

/**
 * Platform-specific clipboard and external URL operations required by the UI.
 */
interface PlatformActions {
    /**
     * Copies [text] to the operating system clipboard.
     *
     * @param text Value to copy.
     */
    fun copyToClipboard(text: String)

    /**
     * Requests that [url] be opened by the operating system.
     *
     * @param url Validated HTTP(S) destination.
     * @param onResult Receives whether a handler successfully accepted the request.
     */
    fun openUrl(url: String, onResult: (Boolean) -> Unit)

    /**
     * Requests that [text] be shared via the platform share sheet.
     *
     * @param text Text message to share.
     */
    fun shareText(text: String)
}

/**
 * Remembers the current platform implementation of [PlatformActions].
 */
@Composable
expect fun rememberPlatformActions(): PlatformActions
