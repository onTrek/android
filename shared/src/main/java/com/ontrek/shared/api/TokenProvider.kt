package com.ontrek.shared.api

interface TokenProvider {
    fun getToken(): String?
}
