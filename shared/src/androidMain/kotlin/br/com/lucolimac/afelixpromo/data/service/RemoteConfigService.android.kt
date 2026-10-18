package br.com.lucolimac.afelixpromo.data.service

import br.com.lucolimac.afelixpromo.shared.R
import br.com.lucolimac.afelixpromo.presentation.catalog.StoreDisplayMode
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

/**
 * Fetches store display mode from Android Firebase Remote Config.
 */
internal actual suspend fun fetchStoreDisplayMode(): StoreDisplayMode {
    val remoteConfig = FirebaseRemoteConfig.getInstance()
    val configSettings = FirebaseRemoteConfigSettings.Builder()
        .setMinimumFetchIntervalInSeconds(3600)
        .build()
    remoteConfig.setConfigSettingsAsync(configSettings)
    remoteConfig.setDefaultsAsync(RemoteConfigDefaults.defaults)
    
    return try {
        remoteConfig.fetchAndActivate().await()
        val value = remoteConfig.getString("store_display_mode")
        StoreDisplayMode.fromValue(value)
    } catch (_: Exception) {
        StoreDisplayMode.LIST
    }
}
