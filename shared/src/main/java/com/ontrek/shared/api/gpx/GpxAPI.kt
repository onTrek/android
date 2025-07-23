package com.ontrek.shared.api.gpx

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


fun downloadGpx(
    gpxID: Int,
    onSuccess: (ByteArray) -> Unit,
    onError: (String) -> Unit
) {
    RetrofitClient.api.downloadGPX(gpxID).enqueue(object : Callback<ResponseBody> {
        override fun onResponse(
            call: Call<ResponseBody>,
            response: Response<ResponseBody>
        ) {
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()

                if (body == null) {
                    Log.e("DownloadTrack", "Response body is null")
                    return
                }

                Log.d("DownloadTrack", "File found successfully")
                onSuccess(body.bytes())
            } else {
                Log.e("DownloadTrack", "Server returned error")
                onError("${response.code()} - ${response.message()}")
            }
        }

        override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
            Log.e("DownloadTrack", "Error: " + t.message)
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

