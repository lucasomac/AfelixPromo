package br.com.lucolimac.afelixpromo.presentation.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Displays an empty, loading, or retryable catalog message.
 *
 * @param title Short heading for the current state.
 * @param message Supporting text for the current state.
 * @param modifier Modifier applied to the message container.
 * @param actionLabel Optional label for the action button.
 * @param action Optional callback for the action button.
 */
@Composable
internal fun EmptyCatalogMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    action: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium)
        if (action != null && actionLabel != null) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = action) {
                Text(actionLabel)
            }
        }
    }
}
