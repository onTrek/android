package com.ontrek.shared.api.groups

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.shared.data.GroupMember
import com.ontrek.shared.data.MemberInfo
import com.ontrek.shared.data.MemberInfoUpdate
import com.ontrek.shared.data.MessageResponse

fun addMemberInGroup(
    groupID: Int,
    userID: String,
    onSuccess: (GroupMember?) -> Unit,
    onError: (String) -> Unit,
) {
    RetrofitClient.api.addMemberToGroup(groupID, userID).enqueue(object : Callback<GroupMember> {
        override fun onResponse(call: Call<GroupMember>, response: Response<GroupMember>) {
            if (response.isSuccessful) {
                Log.d("API Group Member", "API Success")
                val groupMember = response.body()
                onSuccess(groupMember)
            } else {
                Log.e("API Group Member", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<GroupMember>, t: Throwable) {
            Log.e("API Group Member", "API Error: ${t.message}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun removeMemberFromGroup(
    groupID: Int,
    userID: String? = null,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    RetrofitClient.api.removeMemberFromGroup(groupID, userID).enqueue(object : Callback<Void> {
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
            Log.e("API Group Member", "API Error: ${t.message}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })

}

fun getGroupMembers(
    groupId: Int,
    onSuccess: (List<MemberInfo>?) -> Unit,
    onError: (String) -> Unit
) {
    RetrofitClient.api.getGroupMembers(groupId).enqueue(object : Callback<List<MemberInfo>> {
        override fun onResponse(call: Call<List<MemberInfo>>, response: Response<List<MemberInfo>>) {
            if (response.isSuccessful) {
                Log.d("API Group Members", "API Success")
                val members = response.body()
                onSuccess(members)
            } else {
                Log.e("API Group Members", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<List<MemberInfo>>, t: Throwable) {
            Log.e("API Group Members", "API Error: ${t.message}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}

fun updateMemberLocation(
    groupId: Int,
    memberInfo: MemberInfoUpdate,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    RetrofitClient.api.updateMemberLocation(groupId, memberInfo).enqueue(object : Callback<MessageResponse> {
        override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
            if (response.isSuccessful) {
                Log.d("API Group Member Location", "API Success")
                onSuccess()
            } else {
                Log.e("API Group Member Location", "API Error: ${response.code()}")
                onError("API Error: ${response.code()}")
            }
        }

        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
            Log.e("API Group Member Location", "API Error: ${t.message}")
            onError("API Error: ${t.message ?: "Unknown error"}")
        }
    })
}