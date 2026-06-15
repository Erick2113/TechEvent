package com.example.techevent.data

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface TechEventApiService {
    @GET("344b23638686f3d6058f")
    suspend fun obtenerEventos(): Response<List<EventoNetwork>>
}

object RetrofitClient {
    private const val BASE_URL = "https://api.npoint.io/"

    val apiService: TechEventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TechEventApiService::class.java)
    }
}