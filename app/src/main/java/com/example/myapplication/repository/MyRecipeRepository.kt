package com.example.myapplication.repository

import android.util.Log
import com.example.myapplication.model.MyRecipe
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class MyRecipeRepository {

    private val client = HttpClient(CIO) {
        expectSuccess = true


        install(ContentNegotiation) {
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

    private val url = "https://6abcf8005121d616d90c97b1.mockapi.io/recipesphere/recipes"

    suspend fun getRecipes(): List<MyRecipe> {
        return client.get(url).body()
    }

    suspend fun createRecipe(recipe: MyRecipe): MyRecipe {
        return client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(recipe)
        }.body()
    }

    suspend fun deleteRecipe(id: String) {
        client.delete("$url/$id")
    }
}