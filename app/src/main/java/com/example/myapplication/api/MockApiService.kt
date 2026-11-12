/*package com.example.myapplication.api

import com.example.myapplication.model.MyRecipe
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MockApiService {

    @GET("recipes")
    suspend fun getRecipes(): List<MyRecipe>

    @POST("recipes")
    suspend fun createRecipe(@Body recipe: MyRecipe): MyRecipe

    @PUT("recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: String,
        @Body recipe: MyRecipe
    ): MyRecipe

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: String): MyRecipe
}*/