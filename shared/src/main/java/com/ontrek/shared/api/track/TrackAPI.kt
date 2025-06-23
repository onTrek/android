package com.ontrek.shared.api.track

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun fetchData(onSuccess: (List<Track>?) -> Unit, onError: (String) -> Unit, token: String) {
    RetrofitClient.api.getData(token).enqueue(object : Callback<List<Track>> {
        override fun onResponse(call: Call<List<Track>>, response: Response<List<Track>>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("WearOS", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("WearOS", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(
            call: Call<List<Track>?>,
            t: Throwable
        ) {
            Log.e("WearOS", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}