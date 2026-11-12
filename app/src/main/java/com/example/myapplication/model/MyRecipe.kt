package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class MyRecipe(
    val id: String? = null,
    val title: String = "",
    val image: String = "",
    val ingredients: String = "",
    val instructions: String = "",
    val readyInMinutes: Int = 0
)