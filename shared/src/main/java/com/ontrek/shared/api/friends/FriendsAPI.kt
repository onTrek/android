package com.ontrek.shared.api.friends

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.UserMinimal

fun getFriends(token: String, onSuccess: (List<UserMinimal>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getFriends(token).enqueue(object : retrofit2.Callback<List<UserMinimal>> {
        override fun onResponse(call: retrofit2.Call<List<UserMinimal>>, response: retrofit2.Response<List<UserMinimal>>) {
            if (response.isSuccessful) {
                val data = response.body()
                onSuccess(data)
            } else {
                Log.e("API Friends", "Error: ${response.code()} - ${response.message()}")
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<UserMinimal>>, t: Throwable) {
            Log.e("API Friends", "Error: ${t.message ?: "Unknown error"}")
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}

fun deleteFriend(token: String, id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.deleteFriend(token, id).enqueue(object : retrofit2.Callback<com.ontrek.shared.data.MessageResponse> {
        override fun onResponse(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, response: retrofit2.Response<com.ontrek.shared.data.MessageResponse>) {
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "UserMinimal deleted"
                onSuccess(message)
            } else {
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, t: Throwable) {
            Log.e("API Friends", "Error: ${t.message ?: "Unknown error"}")
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}