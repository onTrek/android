package com.ontrek.shared.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
* Retrofit instance class
*/
class ApiClient(tokenProvider: TokenProvider) {
    private val apiService: ApiService

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://ontrek.popipopi.win:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        apiService = retrofit.create(ApiService::class.java)
    }

    fun getApiService(): ApiService = apiService
}