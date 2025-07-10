package com.ontrek.mobile

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ontrek.mobile.data.PreferencesStore
import com.ontrek.shared.api.RetrofitClient

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "setting"
)

class MobileApplication : Application() {
    lateinit var preferencesStore: PreferencesStore
    override fun onCreate() {
        super.onCreate()
        preferencesStore = PreferencesStore(dataStore)

        // Inizializza RetrofitClient con il tokenManager per gestire l'interceptor
        val tokenManager = SharedTokenManager(preferencesStore)
        RetrofitClient.init(tokenManager)
    }

    // Per accedere a preferencesStore da altre parti dell'app
    companion object {
        lateinit var instance: MobileApplication
            private set
    }

    init {
        instance = this
    }
}