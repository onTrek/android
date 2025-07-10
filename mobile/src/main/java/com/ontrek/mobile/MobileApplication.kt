package com.ontrek.mobile

import android.app.Application
import com.ontrek.shared.api.RetrofitClient

class MobileApplication : Application() {
    companion object {
        lateinit var storeApplication: StoreApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Crea StoreApplication
        storeApplication = StoreApplication()
        storeApplication.onCreate()

        // Crea il TokenManager con PreferencesStore da storeApplication
        val tokenManager = SharedTokenManager(storeApplication.preferencesStore)

        // Inizializza RetrofitClient
        RetrofitClient.init(tokenManager)
    }
}