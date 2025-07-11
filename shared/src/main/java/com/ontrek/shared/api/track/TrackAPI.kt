package com.ontrek.shared.api.track

import android.util.Log
import androidx.compose.ui.platform.LocalDensity
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.MessageResponse
import com.ontrek.shared.data.Track
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

fun getTracks(onSuccess: (List<Track>?) -> Unit, onError: (String) -> Unit, token: String) {
    RetrofitClient.api.getTracks(token).enqueue(object : Callback<List<Track>> {
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

fun uploadTrack(
    gpxFileBytes: ByteArray,
    titleTrack: String,
    onSuccess: (MessageResponse?) -> Unit,
    onError: (String) -> Unit,
    token: String
) {
    val titlePart = RequestBody.create(MultipartBody.FORM, titleTrack)
    val requestFile = RequestBody.create(MediaType.parse("application/gpx+xml"), gpxFileBytes)
    val filePart = MultipartBody.Part.createFormData("file", titleTrack, requestFile)
    Log.d("API Track", "Uploading track: $titleTrack with token: $token")
    RetrofitClient.api.uploadTrack(token, titlePart, filePart).enqueue(object : Callback<MessageResponse> {
        override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
            if (response.isSuccessful) {
                Log.d("API Track", "Upload Success: ${response.body()}")
                onSuccess(response.body())
            } else {
                Log.e("API Track", "Upload Error: ${response.code()}")
                onError("Upload Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
            Log.e("API Track", "Upload Error: ${t.toString()}")
            onError("Upload Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun deleteTrack(
    id: String,
    onSuccess: (MessageResponse?) -> Unit,
    onError: (String) -> Unit,
    token: String
) {
    RetrofitClient.api.deleteTrack(id, token).enqueue(object : Callback<MessageResponse> {
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

fun downloadTrack(
    id: String,
    onSuccess: (ResponseBody?) -> Unit,
    onError: (String) -> Unit,
    token: String
) {
    RetrofitClient.api.downloadTrack(id, "Bearer $token").enqueue(object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                Log.d("API Track", "Download Success")
                onSuccess(response.body())
            } else {
                Log.e("API Track", "Download Error: ${response.code()}")
                onError("Download Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e("API Track", "Download Error: ${t.toString()}")
            onError("Download Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun getMapTrack(
    id: String,
    onSuccess: (ResponseBody?) -> Unit,
    onError: (String) -> Unit,
    token: String
) {
    RetrofitClient.api.getMapTrack(id, "Bearer $token").enqueue(object : Callback<ResponseBody> {
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