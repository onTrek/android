import com.ontrek.shared.data.GpxResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import com.ontrek.shared.data.Login
import com.ontrek.shared.data.TokenResponse

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://ontrek.popipopi.win:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)
}

interface ApiService {
    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("gpx/")
    fun getData(@Header("Authorization") token: String): Call<GpxResponse>

    @Headers("Content-Type: application/json;charset=UTF-8")
    @POST("/auth/login")
    fun login(@Body loginBody: Login): Call<TokenResponse>
}