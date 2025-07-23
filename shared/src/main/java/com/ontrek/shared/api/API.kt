package com.ontrek.shared.api

import com.ontrek.shared.data.Login
import com.ontrek.shared.data.MessageResponse
import com.ontrek.shared.data.Signup
import com.ontrek.shared.data.TokenResponse
import com.ontrek.shared.data.Friend
import com.ontrek.shared.data.FriendRequest
import com.ontrek.shared.data.Track
import com.ontrek.shared.data.Profile
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming

object RetrofitClient {
    private lateinit var tokenProvider: TokenProvider

    fun initialize(tokenProvider: TokenProvider) {
        this.tokenProvider = tokenProvider
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://ontrek.popipopi.win:3000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService by lazy { retrofit.create(ApiService::class.java) }
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
    fun getProfile(): Call<Profile>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @DELETE ("/profile")
    fun deleteProfile(): Call<MessageResponse>

    @Multipart
    @PUT("/profile/image")
    fun uploadImageProfile(@Part imageFile: MultipartBody.Part): Call<MessageResponse>

    // ------- USERS ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("/users/{id}/image")
    fun getImageProfile(@Path("id") id: String): Call<ResponseBody>

    // ------- GPX ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("gpx/")
    fun getTracks(): Call<List<Track>>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("gpx/{id}")
    fun getTrack(@Path("id") id: String): Call<Track>

    @Multipart
    @POST("gpx/")
    fun uploadTrack(@Part("title") title: RequestBody, @Part gpxFile: MultipartBody.Part): Call<MessageResponse>

    @DELETE("gpx/{id}")
    fun deleteTrack(@Path("id") id: String, ): Call<MessageResponse>

    @Streaming
    @GET("gpx/{id}/map")
    fun getMapTrack(@Path("id") id: String, ): Call<ResponseBody>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("/gpx/{id}/download")
    fun downloadGPX(@Path("id") gpxID: Int): Call<ResponseBody>


    // ------- FRIENDS ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("friends/")
    fun getFriends(): Call<List<Friend>>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @DELETE("friends/{id}")
    fun deleteFriend(@Path("id") id: String): Call<MessageResponse>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("search")
    fun searchUser(@Query("query") search: String, @Query("friendOnly") friendOnly: Boolean = false): Call<List<Friend>>

    // ------- FRIEND REQUESTS ---------
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("friends/requests/received/")
    fun getFriendRequests(): Call<List<FriendRequest>>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("friends/requests/sent/")
    fun getSentFriendRequests(): Call<List<FriendRequest>>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @PUT("friends/requests/{id}")
    fun acceptFriendRequest(@Path("id") id: String): Call<MessageResponse>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("friends/requests/{id}")
    fun postFriendRequest(@Path("id") id: String): Call<MessageResponse>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @DELETE("friends/requests/{id}")
    fun deleteFriendRequest(@Path("id") id: String): Call<MessageResponse>
}