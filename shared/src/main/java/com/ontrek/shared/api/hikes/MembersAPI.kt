package com.ontrek.shared.api.hikes

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.MemberInfo
import com.ontrek.shared.data.MemberInfoUpdate

fun addMemberInGroup(
    id: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    token: String
) {
    RetrofitClient.api.addMemberToGroup(token, id).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("API Group Member", "API Success")
                onSuccess()
            } else {
                Log.e("API Group Member", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("API Group Member", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun removeMemberFromGroup(
    id: Int,
    userID: String? = null,
    token: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    RetrofitClient.api.removeMemberFromGroup(token, id, userID).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("API Group Member", "API Success")
                onSuccess()
            } else {
                Log.e("API Group Member", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("API Group Member", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })

}

fun getGroupMembers(
    id: Int,
    token: String,
    onSuccess: (List<MemberInfo>?) -> Unit,
    onError: (String) -> Unit
) {
    RetrofitClient.api.getGroupMembers(token, id).enqueue(object : Callback<List<MemberInfo>> {
        override fun onResponse(call: Call<List<MemberInfo>>, response: Response<List<MemberInfo>>) {
            if (response.isSuccessful) {
                Log.d("API Group Members", "API Success")
                onSuccess(response.body())
            } else {
                Log.e("API Group Members", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<List<MemberInfo>>, t: Throwable) {
            Log.e("API Group Members", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun updateMemberLocation(
    id: Int,
    memberInfo: MemberInfoUpdate,
    token: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    RetrofitClient.api.updateMemberLocation(token, id, memberInfo).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("API Group Member Location", "API Success")
                onSuccess()
            } else {
                Log.e("API Group Member Location", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("API Group Member Location", "API Error: ${t.toString()}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}