package com.ontrek.mobile

import com.ontrek.mobile.data.PreferencesStore
import com.ontrek.shared.api.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SharedTokenManager(private val preferencesStore: PreferencesStore) : TokenManager {

    override fun fetchAuthToken(): String? {
        return runBlocking {
            preferencesStore.currentToken.first()
        }
    }
}