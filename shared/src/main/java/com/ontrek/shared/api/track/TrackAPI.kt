package com.ontrek.shared.api.track

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun fetchData(callback : (List<Track>?) -> Unit, token : String) {
    RetrofitClient.api.getData(token).enqueue(object : Callback<List<Track>> {
        override fun onResponse(call: Call<List<Track>>, response: Response<List<Track>>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("WearOS", "API Success: $data")
                callback(data)
            } else {
                Log.e("WearOS", "API Error: ${response.code()}")
            }
        }

        override fun onFailure(
            call: Call<List<Track>?>,
            t: Throwable
        ) {
            Log.e("WearOS", "API Error: ${t.toString()}")
        }
    })
}