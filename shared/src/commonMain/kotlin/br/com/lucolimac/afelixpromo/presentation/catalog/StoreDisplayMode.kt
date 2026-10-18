package br.com.lucolimac.afelixpromo.presentation.catalog

/**
 * Defines the available store catalog layout display modes.
 */
enum class StoreDisplayMode(val value: String) {
    /** Standard single-column vertical list layout. */
    LIST("list"),
    /** Multi-column card grid layout. */
    GRID("grid");

    companion object {
        /**
         * Resolves a [StoreDisplayMode] from its string representation, defaulting to [LIST].
         */
        fun fromValue(value: String?): StoreDisplayMode {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: LIST
        }
    }
}
