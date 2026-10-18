package br.com.lucolimac.afelixpromo.data.service

import br.com.lucolimac.afelixpromo.presentation.catalog.StoreDisplayMode
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import swiftPMImport.br.com.lucolimac.afelixpromo.shared.FIRApp
import swiftPMImport.br.com.lucolimac.afelixpromo.shared.FIRRemoteConfig
import kotlin.coroutines.resume

/**
 * Fetches store display mode from iOS Firebase Remote Config.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual suspend fun fetchStoreDisplayMode(): StoreDisplayMode {
    if (FIRApp.defaultApp() == null) {
        if (NSBundle.mainBundle.pathForResource("GoogleService-Info", ofType = "plist") == null) {
            throw IllegalStateException("O arquivo GoogleService-Info.plist não está no bundle do app iOS.")
        }
        FIRApp.configure()
    }
    
    val remoteConfig = FIRRemoteConfig.remoteConfig()
    
    val nsDefaults = mutableMapOf<Any?, Any?>()
    RemoteConfigDefaults.defaults.forEach { (key, value) ->
        nsDefaults[key] = value
    }
    remoteConfig.setDefaults(nsDefaults)
    
    return suspendCancellableCoroutine { continuation ->
        remoteConfig.fetchWithCompletionHandler { _, error: NSError? ->
            if (error != null) {
                continuation.resume(StoreDisplayMode.LIST)
            } else {
                remoteConfig.activateWithCompletion { _, activateError ->
                    if (activateError != null) {
                        continuation.resume(StoreDisplayMode.LIST)
                    } else {
                        val rawValue = remoteConfig.configValueForKey("store_display_mode").stringValue
                        continuation.resume(StoreDisplayMode.fromValue(rawValue))
                    }
                }
            }
        }
    }
}
