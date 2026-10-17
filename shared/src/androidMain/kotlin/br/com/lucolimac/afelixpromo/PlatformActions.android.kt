package br.com.lucolimac.afelixpromo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import br.com.lucolimac.afelixpromo.shared.R

/**
 * Remembers Android clipboard and URL actions using the current context.
 */
@Composable
actual fun rememberPlatformActions(): PlatformActions {
    val context = LocalContext.current
    return remember(context) {
        object : PlatformActions {
            /** Writes plain text to Android's primary clipboard. */
            override fun copyToClipboard(text: String) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.clipboard_label), text))
            }

            /** Launches an Android view intent and reports whether it was accepted. */
            override fun openUrl(url: String, onResult: (Boolean) -> Unit) {
                val uri = url.toUri()
                if (uri.host.isNullOrBlank()) {
                    onResult(false)
                    return
                }
                val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(intent)
                    onResult(true)
                } catch (_: ActivityNotFoundException) {
                    onResult(false)
                } catch (_: SecurityException) {
                    onResult(false)
                }
            }
        }
    }
}
