package br.com.lucolimac.afelixpromo

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import afelixpromo.shared.generated.resources.Res
import afelixpromo.shared.generated.resources.back
import org.jetbrains.compose.resources.stringResource

/**
 * Displays an Android-style arrow control to return to the previous app screen.
 *
 * @param onClick Called when the user activates the control.
 */
@Composable
actual fun BackNavigationButton(onClick: () -> Unit) {
    val backLabel = stringResource(Res.string.back)
    IconButton(
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = backLabel },
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
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
