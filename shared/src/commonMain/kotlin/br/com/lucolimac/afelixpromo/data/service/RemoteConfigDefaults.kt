package br.com.lucolimac.afelixpromo.data.service

/**
 * Default values for Firebase Remote Config parameters used across platforms.
 */
internal object RemoteConfigDefaults {
    /** Default store catalog layout display mode. */
    const val STORE_DISPLAY_MODE = "list"

    /** Map of all default Remote Config key-value pairs. */
    val defaults: Map<String, Any> = mapOf(
        "store_display_mode" to STORE_DISPLAY_MODE
    )
}
