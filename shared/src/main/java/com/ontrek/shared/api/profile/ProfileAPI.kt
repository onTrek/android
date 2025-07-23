package com.ontrek.shared.api.profile

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.MessageResponse
import com.ontrek.shared.data.Profile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun getProfile(onSuccess : (Profile?) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.getProfile().enqueue(object : Callback<Profile> {
        override fun onResponse(call: Call<Profile>, response: Response<Profile>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("Profile", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("Profile", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(
            call: Call<Profile?>,
            t: Throwable
        ) {
            Log.e("Profile", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun getImageProfile(id: String,
                    onSuccess: (ByteArray) -> Unit,
                    onError: (String) -> Unit) {
    RetrofitClient.api.getImageProfile(id).enqueue(object: Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                val body = response.body()

                if (body == null) {
                    Log.e("DownloadTrack", "Response body is null")
                    return
                }

                Log.d("DownloadImageProfile", "File found successfully")
                onSuccess(body.bytes())
            } else {
                Log.e("DownloadImageProfile", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e("DownloadImageProfile", "API Error: ${t.message}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun uploadImageProfile(
    imageBytes: ByteArray,
    filename: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val requestFile = imageBytes.toRequestBody("image/*".toMediaTypeOrNull(), 0, imageBytes.size)
    val filePart = MultipartBody.Part.createFormData("file", filename, requestFile)
    RetrofitClient.api.uploadImageProfile(filePart).enqueue(object : Callback<MessageResponse> {
        override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
            if (response.isSuccessful) {
                val data = response.body()?.message ?: "Image updated successfully"
                Log.d("UploadImageProfile", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("UploadImageProfile", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
            Log.e("UploadImageProfile", "API Error: ${t.message}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun deleteProfile(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    RetrofitClient.api.deleteProfile().enqueue(object : Callback<MessageResponse> {
        override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
            if (response.isSuccessful) {
                val data = response.body()?.message ?: "Profile deleted successfully"
                Log.d("Profile", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("Profile", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(
            call: Call<MessageResponse>,
            t: Throwable
        ) {
            Log.e("Profile", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}