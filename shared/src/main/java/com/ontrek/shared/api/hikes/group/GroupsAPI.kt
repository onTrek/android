package com.ontrek.shared.api.hikes.group

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.GroupInfoResponseDoc
import com.ontrek.shared.data.MessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//@Headers("Content-Type: application/json;charset=UTF-8")
//@GET("/groups/")
//fun getGroups(@Header("Bearer") token: String): Call<List<GroupDoc>>
//
//@Headers("Content-Type: application/json;charset=UTF-8")
//@GET("/groups/{id}")
//fun getGroupInfo(@Header("Bearer") token: String, @Path("id") id: Int): Call<GroupInfoResponseDoc>
//
//@Headers("Content-Type: application/json;charset=UTF-8")
//@DELETE("/groups/{id}")
//fun deleteGroup(@Header("Bearer") token: String, @Path("id") id: Int): Call<MessageResponse>
//
//@Headers("Content-Type: application/json;charset=UTF-8")
//@PATCH("/groups/{id}/gpx")
//fun changeGPXInGroup(@Header("Bearer") token: String, @Path("id") id: Int, @Body trackId: FileID): Call<MessageResponse>
//
//@Headers("Content-Type: application/json;charset=UTF-8")
//@POST("/groups/")
//fun createGroup(@Header("Bearer") token: String, @Body group: GroupIDCreation): Call<GroupID>

fun getGroupInfo(
    id: Int,
    onSuccess: (GroupInfoResponseDoc?) -> Unit,
    onError: (String) -> Unit,
    token: String
) {
    RetrofitClient.api.getGroupInfo(token, id).enqueue(object : Callback<GroupInfoResponseDoc> {
        override fun onResponse(call: Call<GroupInfoResponseDoc>, response: Response<GroupInfoResponseDoc>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("API Group", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("API Group", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<GroupInfoResponseDoc>, t: Throwable) {
            Log.e("API Group", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun deleteGroup(
    id: Int,
    onSuccess: (MessageResponse?) -> Unit,
    onError: (String) -> Unit,
    token: String
) {
    RetrofitClient.api.deleteGroup(token, id).enqueue(object : Callback<MessageResponse> {
        override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("API Group", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("API Group", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
            Log.e("API Group", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}




