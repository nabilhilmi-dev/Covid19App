package com.nabil.covid_19app.Network

import com.nabil.covid_19app.Model.AllNegara
import com.nabil.covid_19app.Model.InfoNegara
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface ApiService{
    @GET("summary")
    fun  getAllNegara(): Call<AllNegara>

}

interface InfoService{
    @GET
    fun getInfoService(@Url Url: String): Call<List<InfoNegara>>
}

object RetrofitBuilder{
    private val okHttp = OkHttpClient().newBuilder()
        .connectTimeout(15,TimeUnit.SECONDS)
        .readTimeout(10,TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()


    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.covid19api.com/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

