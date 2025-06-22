package com.ontrek.wear

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.ontrek.wear.data.TokenStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "setting"
)

class StoreApplication: Application() {

    lateinit var tokenStore: TokenStore
    override fun onCreate() {
        super.onCreate()
        tokenStore = TokenStore(dataStore)
    }
}