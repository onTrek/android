package com.ontrek.shared.api.profile

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.MessageResponse
import com.ontrek.shared.data.Profile
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun getProfile(token : String, onSuccess : (Profile?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getProfile(token).enqueue(object : Callback<Profile> {
        override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("Profile", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("Profile", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(
            call: Call<Profile?>,
            t: Throwable
        ) {
            Log.e("Profile", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun deleteProfile(token: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.deleteProfile(token).enqueue(object : Callback<MessageResponse> {
        override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
            if (response.isSuccessful) {
                val data = response.body()?.message ?: "Profile deleted successfully"
                Log.d("Profile", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("Profile", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(
            call: Call<MessageResponse>,
            t: Throwable
        ) {
            Log.e("Profile", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}