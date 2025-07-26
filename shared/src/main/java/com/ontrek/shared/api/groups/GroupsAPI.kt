package com.ontrek.shared.api.groups

import android.util.Log
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.FileID
import com.ontrek.shared.data.GroupDoc
import com.ontrek.shared.data.GroupID
import com.ontrek.shared.data.GroupIDCreation
import com.ontrek.shared.data.GroupInfoResponseDoc
import com.ontrek.shared.data.MessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun getGroupInfo(
    id: Int,
    onSuccess: (GroupInfoResponseDoc?) -> Unit,
    onError: (String) -> Unit,
) {
    RetrofitClient.api.getGroupInfo(id).enqueue(object : Callback<GroupInfoResponseDoc> {
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
) {
    RetrofitClient.api.deleteGroup(id).enqueue(object : Callback<MessageResponse> {
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

fun getGroups(
    onSuccess: (List<GroupDoc>?) -> Unit,
    onError: (String) -> Unit,
) {
    RetrofitClient.api.getGroups().enqueue(object : Callback<List<GroupDoc>> {
        override fun onResponse(call: Call<List<GroupDoc>>, response: Response<List<GroupDoc>>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("API Group", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("API Group", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<List<GroupDoc>>, t: Throwable) {
            Log.e("API Group", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun createGroup(
    group: GroupIDCreation,
    onSuccess: (GroupID?) -> Unit,
    onError: (String) -> Unit,
) {
    RetrofitClient.api.createGroup(group).enqueue(object : Callback<GroupID> {
        override fun onResponse(call: Call<GroupID>, response: Response<GroupID>) {
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("API Group Create", "API Success: $data")
                onSuccess(data)
            } else {
                Log.e("API Group", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<GroupID>, t: Throwable) {
            Log.e("API Group", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun changeGPXInGroup(
    id: Int,
    trackId: Int,
    onSuccess: (MessageResponse?) -> Unit,
    onError: (String) -> Unit,
) {
    RetrofitClient.api.changeGPXInGroup(id, FileID(trackId)).enqueue(object : Callback<MessageResponse> {
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