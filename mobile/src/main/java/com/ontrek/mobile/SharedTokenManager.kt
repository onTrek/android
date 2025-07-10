package com.ontrek.mobile

import android.util.Log
import com.ontrek.mobile.data.PreferencesStore
import com.ontrek.shared.api.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SharedTokenManager(private val preferencesStore: PreferencesStore) : TokenManager {

    // Questa funzione viene chiamata da RetrofitClient per ottenere il token
    // viene chiamata dall'API create in Shared, e serve runBlocking per ottenere il token in modo sincrono
    // in modo che RetrofitClient possa usarlo immediatamente.
    // Non è consigliato usare runBlocking in un'applicazione Android, ma in questo caso è necessario
    // perché RetrofitClient si aspetta un token sincrono.
    // In un'applicazione reale, si dovrebbe usare un approccio asincrono per
    // ottenere il token, ma in questo caso è accettabile perché il token non cambia
    // frequentemente e non è necessario aggiornare l'interfaccia utente in tempo reale.
    // E poi non c'ho voglia di usare coroutines in questo momento. (poi pensiamo meglio sta cosa, intanto serve farlo funzionare)
    override fun fetchAuthToken(): String? {
        return runBlocking {
            preferencesStore.currentToken.first().takeIf { it.isNotEmpty() }
        }
    }
}