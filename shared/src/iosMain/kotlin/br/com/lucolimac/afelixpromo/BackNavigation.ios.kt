package br.com.lucolimac.afelixpromo

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import afelixpromo.shared.generated.resources.Res
import afelixpromo.shared.generated.resources.back
import org.jetbrains.compose.resources.stringResource

/**
 * Preserves the existing text-based return control on iOS.
 *
 * @param onClick Called when the user activates the control.
 */
@Composable
actual fun BackNavigationButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(stringResource(Res.string.back))
    }
}

/**
 * Leaves system back handling to the existing iOS navigation behavior.
 *
 * @param enabled Unused because this app does not register a system back handler on iOS.
 * @param onBack Unused because this app does not register a system back handler on iOS.
 */
@Composable
actual fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
