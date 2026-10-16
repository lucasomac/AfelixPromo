package br.com.lucolimac.afelixpromo

import androidx.activity.compose.BackHandler
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Displays an Android-style arrow control to return to the previous app screen.
 *
 * @param onClick Called when the user activates the control.
 */
@Composable
actual fun BackNavigationButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = "Voltar" },
    ) {
        Text(
            text = "←",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

/**
 * Uses Android's back dispatcher to handle button and gesture navigation.
 *
 * @param enabled Whether the current screen should consume the system back action.
 * @param onBack Called when the system back action is consumed.
 */
@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
