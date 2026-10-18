package br.com.lucolimac.afelixpromo.data.service

import br.com.lucolimac.afelixpromo.presentation.catalog.StoreDisplayMode

/**
 * Fetches the store display mode from the platform's native Firebase Remote Config.
 */
internal expect suspend fun fetchStoreDisplayMode(): StoreDisplayMode
