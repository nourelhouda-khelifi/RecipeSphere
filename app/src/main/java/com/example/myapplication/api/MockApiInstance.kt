/* package com.example.myapplication.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MockApiInstance {

    private const val BASE_URL = "https://6abcf8005121d616d90c97b1.mockapi.io/recipesphere/"

    val api: MockApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MockApiService::class.java)
    }
}*/