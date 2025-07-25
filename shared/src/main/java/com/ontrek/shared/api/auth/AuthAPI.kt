package com.ontrek.shared.api.auth


import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.Login
import com.ontrek.shared.data.MessageResponse
import com.ontrek.shared.data.Signup
import com.ontrek.shared.data.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun login(loginBody : Login, onSuccess : (LoginResponse?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.login(loginBody).enqueue(object : Callback<LoginResponse> {
        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("Auth", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("Auth", "API Error: ${response.code()}, ${response.errorBody()}")
                onError("${response.message()} (Code: ${response.code()})")
            }
        }

        override fun onFailure(
            call: Call<LoginResponse?>,
            t: Throwable
        ) {
            Log.e("Auth", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun signup(signupBody : Signup, onSuccess : (MessageResponse?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.signup(signupBody).enqueue(object : Callback<MessageResponse> {
        override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("Auth", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("Auth", "API Error: ${response.code()}, ${response.errorBody()}")
                onError("${response.message()} (Code: ${response.code()})")
            }
        }

        override fun onFailure(
            call: Call<MessageResponse?>,
            t: Throwable
        ) {
            Log.e("Auth", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}