package com.ontrek.shared.api

import com.ontrek.shared.data.Login
import com.ontrek.shared.data.MessageResponse
import com.ontrek.shared.data.Signup
import com.ontrek.shared.data.TokenResponse
import com.ontrek.shared.data.Friend
import com.ontrek.shared.data.FriendRequest
import com.ontrek.shared.data.Hikes
import com.ontrek.shared.data.Track
import com.ontrek.shared.data.Profile
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

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

    // --------- PROFILE ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("/profile")
    fun getProfile(@Header("Bearer") token: String): Call<Profile>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @DELETE ("/profile")
    fun deleteProfile(@Header("Bearer") token: String): Call<MessageResponse>

    @Multipart
    @PUT("/profile/image")
    fun uploadImageProfile(@Header("Bearer") token: String, @Part imageFile: MultipartBody.Part): Call<MessageResponse>

    // ------- USERS ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("/users/{id}/image")
    fun getImageProfile(@Header("Bearer") token: String, @Path("id") id: String): Call<ResponseBody>

    // ------- GPX ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("gpx/")
    fun getTracks(@Header("Bearer") token: String): Call<List<Track>>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("gpx/{id}")
    fun getTrack(@Header("Bearer") token: String, @Path("id") id: Int): Call<Track>

    @Multipart
    @POST("gpx/")
    fun uploadTrack(@Header("Bearer") token: String, @Part("title") title: RequestBody, @Part gpxFile: MultipartBody.Part): Call<MessageResponse>

    @DELETE("gpx/{id}")
    fun deleteTrack(@Header("Bearer") token: String, @Path("id") id: Int): Call<MessageResponse>

    @Streaming
    @GET("gpx/{id}/map")
    fun getMapTrack(@Header("Bearer") token: String, @Path("id") id: Int): Call<ResponseBody>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("/gpx/{id}/download")
    fun downloadGPX(@Header("Bearer") token: String, @Path("id") gpxID: Int): Call<ResponseBody>

    // ------- HIKES ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("groups/")
    fun getGroups(@Header("Bearer") token: String): Call<List<Hikes>>

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