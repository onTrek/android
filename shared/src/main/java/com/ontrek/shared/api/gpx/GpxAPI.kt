package com.ontrek.shared.api.gpx

import android.util.Log
import com.ontrek.shared.data.GpxResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun fetchData(callback : (GpxResponse?) -> Unit, token : String) {
    RetrofitClient.api.getData(token).enqueue(object : Callback<GpxResponse> {
        override fun onResponse(call: Call<GpxResponse>, response: Response<GpxResponse>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("WearOS", "API Success: $data")
                callback(data)
            } else {
                Log.e("WearOS", "API Error: ${response.errorBody()}")
            }
        }

        override fun onFailure(
            call: Call<GpxResponse?>,
            t: Throwable
        ) {
            Log.e("WearOS", "API Error: ${t.toString()}")
        }
    })
}