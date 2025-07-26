package com.ontrek.shared.api.track

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.MessageResponse
import com.ontrek.shared.data.Track
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun getTracks(onSuccess: (List<Track>?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getTracks().enqueue(object : Callback<List<Track>> {
        override fun onResponse(call: Call<List<Track>>, response: Response<List<Track>>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("API Track", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("API Track", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(
            call: Call<List<Track>?>,
            t: Throwable
        ) {
            Log.e("API Track", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}


fun getTrack(
    id: Int,
    onSuccess: (Track?) -> Unit,
    onError: (String) -> Unit
) {
    RetrofitClient.api.getTrack(id).enqueue(object : Callback<Track> {
        override fun onResponse(call: Call<Track>, response: Response<Track>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("API Track", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("API Track", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<Track>, t: Throwable) {
            Log.e("API Track", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun uploadTrack(
    gpxFileBytes: ByteArray,
    titleTrack: String,
    fileName: String,
    onSuccess: (MessageResponse?) -> Unit,
    onError: (String) -> Unit,
) {
    val titlePart = titleTrack.toRequestBody(MultipartBody.FORM)
    val requestFile = gpxFileBytes.toRequestBody("application/gpx+xml".toMediaTypeOrNull(), 0, gpxFileBytes.size)
    val filePart = MultipartBody.Part.createFormData("file", fileName, requestFile)

    RetrofitClient.api.uploadTrack(titlePart, filePart).enqueue(object : Callback<MessageResponse> {
        override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
            if (response.isSuccessful) {
                Log.d("API Track", "Upload Success: ${response.body()}")
                onSuccess(response.body())
            } else {
                Log.e("API Track", "Upload Error: ${response.message()}")
                var msg = "Upload Error: ${response.code()}"
                msg += when (response.code()) {
                    400 -> "Bad Request: ${response.message()}"
                    401 -> "Unauthorized: ${response.message()}"
                    403 -> "Forbidden: ${response.message()}"
                    404 -> "Not Found: ${response.message()}"
                    500 -> "Internal Server Error: ${response.message()}"
                    else -> "Unexpected Error: ${response.message()}"
                }
                onError("Upload Error: $msg")
            }
        }

        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
            Log.e("API Track", "Upload Error: ${t.toString()}")
            onError("Upload Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun deleteTrack(
    id: Int,
    onSuccess: (MessageResponse?) -> Unit,
    onError: (String) -> Unit,
) {
    RetrofitClient.api.deleteTrack(id).enqueue(object : Callback<MessageResponse> {
        override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
            if (response.isSuccessful) {
                Log.d("API Track", "Delete Success: ${response.body()}")
                onSuccess(response.body())
            } else {
                Log.e("API Track", "Delete Error: ${response.code()}")
                onError("Delete Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
            Log.e("API Track", "Delete Error: ${t.toString()}")
            onError("Delete Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun getMapTrack(
    id: Int,
    onSuccess: (ResponseBody?) -> Unit,
    onError: (String) -> Unit,
) {
    RetrofitClient.api.getMapTrack(id).enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                Log.d("API Track", "Map Download Success")
                onSuccess(response.body())
            } else {
                Log.e("API Track", "Map Download Error: ${response.code()}")
                onError("Map Download Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e("API Track", "Map Download Error: ${t.toString()}")
            onError("Map Download Error: ${t.message ?: "Unknown error"}")
        }
    })
}