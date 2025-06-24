package com.ontrek.shared.api.auth


import android.util.Log
import com.ontrek.shared.data.Login
import com.ontrek.shared.data.MessageResponse
import com.ontrek.shared.data.Signup
import com.ontrek.shared.data.TokenResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun login(loginBody : Login, onSuccess : (TokenResponse?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.login(loginBody).enqueue(object : Callback<TokenResponse> {
        override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("Mobile", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("Mobile", "API Error: ${response.code()}, ${response.errorBody()}")
                onError("${response.code()}")
            }
        }

        override fun onFailure(
            call: Call<TokenResponse?>,
            t: Throwable
        ) {
            Log.e("Mobile", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun signup(signupBody : Signup, onSuccess : (MessageResponse?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.signup(signupBody).enqueue(object : Callback<MessageResponse> {
        override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("Mobile", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("Mobile", "API Error: ${response.code()}, ${response.errorBody()}")
                onError("${response.code()}")
            }
        }

        override fun onFailure(
            call: Call<MessageResponse?>,
            t: Throwable
        ) {
            Log.e("Mobile", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}