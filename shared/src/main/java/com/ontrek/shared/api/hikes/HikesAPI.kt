package com.ontrek.shared.api.hikes

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.Hikes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun getGroups(
    onSuccess: (List<Hikes>?) -> Unit,
    onError: (String) -> Unit,
    token: String
) {
    RetrofitClient.api.getGroups(token).enqueue(object : Callback<List<Hikes>> {
        override fun onResponse(call: Call<List<Hikes>>, response: Response<List<Hikes>>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("API Hikes", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("API Hikes", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(
            call: Call<List<Hikes>?>,
            t: Throwable
        ) {
            Log.e("API Hikes", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}