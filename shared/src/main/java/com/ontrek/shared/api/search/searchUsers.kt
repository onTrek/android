package com.ontrek.shared.api.search

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.UserMinimal

fun searchUsers(token: String, query: String, friendOnly: Boolean = false, onSuccess: (List<UserMinimal>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.searchUser(token, query, friendsOnly = friendOnly).enqueue(object : retrofit2.Callback<List<UserMinimal>> {
        override fun onResponse(call: retrofit2.Call<List<UserMinimal>>, response: retrofit2.Response<List<UserMinimal>>) {
            Log.d("API Search Users", "Request: ${call.request().url}, Query: $query, Friend Only: $friendOnly")
            if (response.isSuccessful) {
                onSuccess(response.body())
            } else {
                onError("API Search Users: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<List<UserMinimal>>, t: Throwable) {
            Log.e("API Search Users", "Error: ${t.message ?: "Unknown error"}")
            onError("API Search Users: ${t.message ?: "Unknown error"}")
        }
    })
}