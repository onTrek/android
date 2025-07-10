package com.ontrek.shared.api

import android.util.Log
import com.ontrek.shared.data.Track
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import com.ontrek.shared.data.Login
import com.ontrek.shared.data.MessageResponse
import com.ontrek.shared.data.Signup
import com.ontrek.shared.data.TokenResponse
import okhttp3.Interceptor
import retrofit2.Callback
import retrofit2.Response


const val BASE_URL = "http://ontrek.popipopi.win:3000/"

/**
 * Interfaccia per la gestione del token di autenticazione da implementare nelle diverse piattaforme.
 */
interface TokenManager {
    fun fetchAuthToken(): String?
}

/**
 * Interceptor per aggiungere il token di autenticazione alle richieste
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val requestBuilder = chain.request().newBuilder()

        tokenManager.fetchAuthToken()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}

object RetrofitClient {
    private var tokenManager: TokenManager? = null
    private var isInitialized = false

    @Synchronized
    fun init(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
        isInitialized = true
        Log.d("RETROFIT", "RetrofitClient inizializzato con successo: $isInitialized")
    }

    @Synchronized
    private fun checkInitialization() {
        if (!isInitialized || tokenManager == null) {
            Log.e("RETROFIT", "RetrofitClient non inizializzato correttamente. isInitialized=$isInitialized, tokenManager=${tokenManager != null}")
            throw IllegalStateException("RetrofitClient non inizializzato correttamente")
        }
    }

    private val okHttpClient by lazy {
        checkInitialization()
        okhttp3.OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager!!))
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy {
        if (!isInitialized) {
            throw IllegalStateException("TokenManager non inizializzato. Chiamare RetrofitClient.init() prima di usare l'API.")
        }
        retrofit.create(ApiService::class.java)
    }
}

interface ApiService {

    // ------- AUTH ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("/auth/login")
    fun login(@Body loginBody: Login): Call<TokenResponse>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("/auth/register")
    fun signup(@Body loginBody: Signup): Call<MessageResponse>

    // ------- USER ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("/profile")
    fun getProfile(): Call<String>



    // ------- GPX ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("gpx/")
    fun getData(@Header("Bearer") token: String): Call<List<Track>>
}

/**
 * Funzione semplificata per testare se l'interceptor aggiunge correttamente il token
 */
fun getDataProfile() {
    try {
        // Ottieni una reference alla chiamata prima di eseguirla
        val call = RetrofitClient.api.getProfile()

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d("TOKEN", "Codice risposta server: ${response.code()}")
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("API", "Errore: ${t.message}")
            }
        })
    } catch (e: IllegalStateException) {
        Log.e("API", "RetrofitClient non inizializzato: ${e.message}")
    } catch (e: Exception) {
        Log.e("API", "Errore in getDataProfile: ${e.message}", e)
    }
}