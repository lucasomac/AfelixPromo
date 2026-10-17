package br.com.lucolimac.afelixpromo.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandPurple,
    onPrimary = BrandLightSurface,
    primaryContainer = BrandPurpleContainer,
    onPrimaryContainer = BrandNavy,
    secondary = BrandGold,
    onSecondary = BrandOnGold,
    secondaryContainer = BrandGoldContainer,
    onSecondaryContainer = BrandOnGold,
    tertiary = BrandMagenta,
    onTertiary = BrandLightSurface,
    background = BrandLightSurface,
    onBackground = BrandNavy,
    surface = BrandLightSurface,
    onSurface = BrandNavy,
    surfaceVariant = BrandPurpleContainer,
    onSurfaceVariant = BrandNavy,
    outline = BrandPurple.copy(alpha = 0.56f),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD9B7FF),
    onPrimary = Color(0xFF3C155E),
    primaryContainer = BrandPurple,
    onPrimaryContainer = Color(0xFFF1DAFF),
    secondary = Color(0xFFFFD166),
    onSecondary = BrandOnGold,
    secondaryContainer = Color(0xFF5B4600),
    onSecondaryContainer = Color(0xFFFFE8A1),
    tertiary = Color(0xFFF4A6DD),
    onTertiary = Color(0xFF571B48),
    background = BrandNavy,
    onBackground = BrandOnDark,
    surface = BrandDarkSurface,
    onSurface = BrandOnDark,
    surfaceVariant = Color(0xFF393449),
    onSurfaceVariant = Color(0xFFD0C5D8),
    outline = Color(0xFF9C8EAA),
)

/**
 * Applies the AfelixPromo brand palette and typography, adapting the color scheme to the system theme.
 *
 * @param darkTheme Whether to render the dark color scheme; defaults to the system setting.
 * @param content UI rendered with the AfelixPromo theme.
 */
@Composable
fun AfelixPromoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AfelixPromoTypography,
        content = content,
    )
}
