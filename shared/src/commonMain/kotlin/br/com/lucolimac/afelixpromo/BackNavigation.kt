package br.com.lucolimac.afelixpromo

import androidx.compose.runtime.Composable

/**
 * Displays the platform-standard control for returning to the previous app screen.
 *
 * @param onClick Called when the user activates the control.
 */
@Composable
expect fun BackNavigationButton(onClick: () -> Unit)

/**
 * Handles the operating system's back action while [enabled] is true.
 *
 * @param enabled Whether the current screen should consume the system back action.
 * @param onBack Called when the system back action is consumed.
 */
@Composable
expect fun SystemBackHandler(enabled: Boolean, onBack: () -> Unit)
