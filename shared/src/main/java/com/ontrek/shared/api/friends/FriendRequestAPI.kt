package com.ontrek.shared.api.friends

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.FriendRequest

fun getFriendRequests(onSuccess: (List<FriendRequest>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getFriendRequests().enqueue(object : retrofit2.Callback<List<FriendRequest>> {
        override fun onResponse(call: retrofit2.Call<List<FriendRequest>>, response: retrofit2.Response<List<FriendRequest>>) {
            if (response.isSuccessful) {
                onSuccess(response.body())
            } else {
                Log.e("API Friends", "Error: ${response.code()} - ${response.message()}")
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<FriendRequest>>, t: Throwable) {
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}

fun getSentFriendRequest(onSuccess: (List<FriendRequest>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getSentFriendRequests().enqueue(object : retrofit2.Callback<List<FriendRequest>> {
        override fun onResponse(call: retrofit2.Call<List<FriendRequest>>, response: retrofit2.Response<List<FriendRequest>>) {
            if (response.isSuccessful) {
                onSuccess(response.body())
            } else {
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<FriendRequest>>, t: Throwable) {
            Log.e("API Friends", "Error: ${t.message ?: "Unknown error"}")
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}


fun acceptFriendRequest(id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.acceptFriendRequest(id).enqueue(object : retrofit2.Callback<com.ontrek.shared.data.MessageResponse> {
        override fun onResponse(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, response: retrofit2.Response<com.ontrek.shared.data.MessageResponse>) {
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Request accepted"
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

fun sendFriendRequest(id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.postFriendRequest(id).enqueue(object : retrofit2.Callback<com.ontrek.shared.data.MessageResponse> {
        override fun onResponse(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, response: retrofit2.Response<com.ontrek.shared.data.MessageResponse>) {
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Request sent"
                onSuccess(message)
            } else {
                Log.e("API Friends", "Error: ${response.code()} - ${response.message()}")
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, t: Throwable) {
            Log.e("API Friends", "Error: ${t.message ?: "Unknown error"}")
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}


fun deleteFriendRequest(id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.deleteFriendRequest(id).enqueue(object : retrofit2.Callback<com.ontrek.shared.data.MessageResponse> {
        override fun onResponse(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, response: retrofit2.Response<com.ontrek.shared.data.MessageResponse>) {
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Request deleted"
                onSuccess(message)
            } else {
                Log.e("API Friends", "Error: ${response.code()} - ${response.message()}")
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, t: Throwable) {
            Log.e("API Friends", "Error: ${t.message ?: "Unknown error"}")
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}
