package com.ontrek.shared.api.friends

import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.Friends

fun getFriends(token: String, search: String? = null, onSuccess: (List<Friends>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getFriends(token, search).enqueue(object : retrofit2.Callback<List<Friends>> {
        override fun onResponse(call: retrofit2.Call<List<Friends>>, response: retrofit2.Response<List<Friends>>) {
            if (response.isSuccessful) {
                val data = response.body()
                onSuccess(data)
            } else {
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<Friends>>, t: Throwable) {
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun getFriendRequests(token: String, onSuccess: (List<Friends>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getFriendRequests(token).enqueue(object : retrofit2.Callback<List<Friends>> {
        override fun onResponse(call: retrofit2.Call<List<Friends>>, response: retrofit2.Response<List<Friends>>) {
            if (response.isSuccessful) {
                val data = response.body()
                onSuccess(data)
            } else {
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<Friends>>, t: Throwable) {
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun acceptFriendRequest(token: String, id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.acceptFriendRequest(token, id).enqueue(object : retrofit2.Callback<com.ontrek.shared.data.MessageResponse> {
        override fun onResponse(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, response: retrofit2.Response<com.ontrek.shared.data.MessageResponse>) {
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Request accepted"
                onSuccess(message)
            } else {
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, t: Throwable) {
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun sendFriendRequest(token: String, id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.sendFriendRequest(token, id).enqueue(object : retrofit2.Callback<com.ontrek.shared.data.MessageResponse> {
        override fun onResponse(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, response: retrofit2.Response<com.ontrek.shared.data.MessageResponse>) {
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Request sent"
                onSuccess(message)
            } else {
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, t: Throwable) {
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}


fun deleteFriendRequest(token: String, id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.deleteFriendRequest(token, id).enqueue(object : retrofit2.Callback<com.ontrek.shared.data.MessageResponse> {
        override fun onResponse(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, response: retrofit2.Response<com.ontrek.shared.data.MessageResponse>) {
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Request deleted"
                onSuccess(message)
            } else {
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, t: Throwable) {
            onError("API Error: ${t.message ?: "Unknown error"}")
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
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, t: Throwable) {
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

