package com.ontrek.shared.api

import okhttp3.Interceptor

class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val token = tokenProvider.getToken()
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Bearer", token)
        }
        return chain.proceed(requestBuilder.build())
    }
}