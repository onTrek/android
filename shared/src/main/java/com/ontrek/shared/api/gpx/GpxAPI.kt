package com.ontrek.shared.api.gpx

import android.content.Context
import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


fun downloadGpx(token: String, gpxID: Int, context: Context, onSuccess : () -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.downloadGPX(token, gpxID).enqueue(object : Callback<ResponseBody> {
        override fun onResponse(
            call: Call<ResponseBody>,
            response: Response<ResponseBody>
        ) {
            if (response.isSuccessful && response.body() != null) {
                Log.d("Download", "File found successfully")
                writeResponseBodyToDisk(response.body(), "$gpxID.gpx", context)
                onSuccess()
            } else {
                Log.e("Download", "Server returned error")
            }
        }

        override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
            Log.e("Download", "Error: " + t.message)
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

private fun writeResponseBodyToDisk(body: ResponseBody?, fileName: String, context: Context) {
    if (body == null) {
        Log.e("File Download", "Response body is null")
        return
    }
    val filename = fileName
    val fileContents = body.bytes()
    //save the file to the app's private storage
    context.openFileOutput(filename, Context.MODE_PRIVATE).use {
        it.write(fileContents)
    }
}

