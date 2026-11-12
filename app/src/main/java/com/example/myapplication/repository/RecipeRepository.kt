package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.BuildConfig
import com.example.myapplication.model.RecipeDetail
import com.example.myapplication.model.RecipeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class RecipeRepository {

    private val client = HttpClient(CIO) {
        expectSuccess = true


        install(ContentNegotiation) {
            // Spoonacular renvoie beaucoup de champs qu'on n'utilise pas : on les ignore
            json(Json { ignoreUnknownKeys = true })
        }

        install(Logging) {
            level = LogLevel.INFO
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("Ktor-Logger", message)
                }
            }
        }
    }

    private val baseUrl = "https://api.spoonacular.com/recipes"

    suspend fun getRecipes(
        offset: Int,
        query: String?,
        cuisine: String?,
        number: Int = 20
    ): RecipeResponse {
        return client.get("$baseUrl/complexSearch") {
            parameter("apiKey", BuildConfig.SPOONACULAR_API_KEY)
            parameter("number", number)
            parameter("offset", offset)
            parameter("query", query)       // un paramètre null est ignoré
            parameter("cuisine", cuisine)
        }.body()
    }

    suspend fun getRecipeDetail(id: Int): RecipeDetail {
        return client.get("$baseUrl/$id/information") {
            parameter("apiKey", BuildConfig.SPOONACULAR_API_KEY)
        }.body()
    }
}