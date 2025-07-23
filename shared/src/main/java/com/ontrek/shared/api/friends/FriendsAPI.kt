package com.ontrek.shared.api.friends

import android.util.Log
import com.ontrek.shared.api.ApiService
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.Friend
import com.ontrek.shared.data.MessageResponse

class FriendRepository(private val apiService: ApiService) {
    fun getFriends(onSuccess: (List<Friend>?) -> Unit, onError: (String) -> Unit) {
        apiService.getFriends().enqueue(object : retrofit2.Callback<List<Friend>> {
            override fun onResponse(
                call: retrofit2.Call<List<Friend>>,
                response: retrofit2.Response<List<Friend>>
            ) {
                Log.d("API Friends", "Request Headers: ${call.request().headers}")

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

    fun deleteFriend(id: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        apiService.deleteFriend(id)
            .enqueue(object : retrofit2.Callback<MessageResponse> {
                override fun onResponse(
                    call: retrofit2.Call<MessageResponse>,
                    response: retrofit2.Response<MessageResponse>
                ) {
                    // Stampa l'header della richiesta per debug
                    if (response.isSuccessful) {
                        val message = response.body()?.message ?: "Friend deleted"
                        onSuccess(message)
                    } else {
                        onError("API Friends: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<MessageResponse>,
                    t: Throwable
                ) {
                    Log.e("API Friends", "Error: ${t.message ?: "Unknown error"}")
                    onError("API Friends: ${t.message ?: "Unknown error"}")
                }
            })
    }
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