package com.ontrek.shared.api.friends

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
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<Friend>>, t: Throwable) {
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}

fun getFriendRequests(token: String, onSuccess: (List<FriendRequest>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getFriendRequests(token).enqueue(object : retrofit2.Callback<List<FriendRequest>> {
        override fun onResponse(call: retrofit2.Call<List<FriendRequest>>, response: retrofit2.Response<List<FriendRequest>>) {
            if (response.isSuccessful) {
                onSuccess(response.body())
            } else {
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<FriendRequest>>, t: Throwable) {
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}

fun getFriendRequestsSend( token: String, onSuccess: (List<FriendRequest>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getFriendRequestsSend(token).enqueue(object : retrofit2.Callback<List<FriendRequest>> {
        override fun onResponse(call: retrofit2.Call<List<FriendRequest>>, response: retrofit2.Response<List<FriendRequest>>) {
            if (response.isSuccessful) {
                onSuccess(response.body())
            } else {
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<FriendRequest>>, t: Throwable) {
            onError("API Friends: ${t.message ?: "Unknown error"}")
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
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, t: Throwable) {
            onError("API Friends: ${t.message ?: "Unknown error"}")
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
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, t: Throwable) {
            onError("API Friends: ${t.message ?: "Unknown error"}")
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
                onError("API Friends: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<com.ontrek.shared.data.MessageResponse>, t: Throwable) {
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
            onError("API Friends: ${t.message ?: "Unknown error"}")
        }
    })
}

