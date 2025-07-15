package com.ontrek.shared.api.friends

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.Friend
import com.ontrek.shared.data.FriendRequest

fun getFriends(token: String, onSuccess: (List<Friend>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getFriends(token).enqueue(object : retrofit2.Callback<List<Friend>> {
        override fun onResponse(call: retrofit2.Call<List<Friend>>, response: retrofit2.Response<List<Friend>>) {
            if (response.isSuccessful) {
                val data = response.body()
                onSuccess(data)
            } else {
                Log.e("API Friends", "Error: ${response.code()} - ${response.message()}")
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<Friend>>, t: Throwable) {
            Log.e("API Friends", "Error: ${t.message ?: "Unknown error"}")
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}

fun deleteFriend(token: String, id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.deleteFriend(token, id).enqueue(object : retrofit2.Callback<com.ontrek.shared.data.MessageResponse> {
        override fun onResponse(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, response: retrofit2.Response<com.ontrek.shared.data.MessageResponse>) {
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Friend deleted"
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

fun searchUsers(token: String, query: String, friendOnly: Boolean = false, onSuccess: (List<Friend>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.searchUser(token, query, friendOnly).enqueue(object : retrofit2.Callback<List<Friend>> {
        override fun onResponse(call: retrofit2.Call<List<Friend>>, response: retrofit2.Response<List<Friend>>) {
            if (response.isSuccessful) {
                onSuccess(response.body())
            } else {
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<Friend>>, t: Throwable) {
            Log.e("API Friends", "Error: ${t.message ?: "Unknown error"}")
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}