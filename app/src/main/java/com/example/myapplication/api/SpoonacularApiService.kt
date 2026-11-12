/*package com.example.myapplication.api
import  com.example.myapplication.model.RecipeResponse
import  com.example.myapplication.model.RecipeDetail

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpoonacularApiService {


    @GET("recipes/complexSearch")
    suspend fun getRecipes(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("query") query: String? = null,
        @Query("cuisine") cuisine: String? = null
    ): RecipeResponse

    @GET("recipes/{id}/information")
    suspend fun getRecipeDetail(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String
    ): RecipeDetail
}*/