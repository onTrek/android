package com.ontrek.shared.api

import com.ontrek.shared.data.Friend
import com.ontrek.shared.data.FriendRequest
import com.ontrek.shared.data.Track
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import com.ontrek.shared.data.Login
import com.ontrek.shared.data.MessageResponse
import com.ontrek.shared.data.Signup
import com.ontrek.shared.data.TokenResponse
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://ontrek.popipopi.win:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}

interface ApiService {

    // ------- AUTH ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("/auth/login")
    fun login(@Body loginBody: Login): Call<TokenResponse>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("/auth/register")
    fun signup(@Body loginBody: Signup): Call<MessageResponse>


    // ------- GPX ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("gpx/")
    fun getData(@Header("Bearer") token: String): Call<List<Track>>


    // ------- FRIENDS ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("friends/")
    fun getFriends(@Header("Bearer") token: String): Call<List<Friend>>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @DELETE("friends/{id}")
    fun deleteFriend(@Header("Bearer") token: String, @Path("id") id: String): Call<MessageResponse>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("search")
    fun searchUser(@Header("Bearer") token: String, @Query("query") search: String, @Query("friendOnly") friendOnly: Boolean = false): Call<List<Friend>>

    // ------- FRIEND REQUESTS ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("friends/requests/received/")
    fun getFriendRequests(@Header("Bearer") token: String): Call<List<FriendRequest>>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("friends/requests/sent/")
    fun getSentFriendRequests(@Header("Bearer") token: String): Call<List<FriendRequest>>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @PUT("friends/requests/{id}")
    fun acceptFriendRequest(@Header("Bearer") token: String, @Path("id") id: String): Call<MessageResponse>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("friends/requests/{id}")
    fun postFriendRequest(@Header("Bearer") token: String, @Path("id") id: String): Call<MessageResponse>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @DELETE("friends/requests/{id}")
    fun deleteFriendRequest(@Header("Bearer") token: String, @Path("id") id: String): Call<MessageResponse>
}