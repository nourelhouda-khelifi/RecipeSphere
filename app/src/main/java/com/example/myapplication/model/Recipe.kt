package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: Int,
    val title: String,
    val image: String = ""
)

@Serializable
data class RecipeResponse(
    val results: List<Recipe>,
    val offset: Int = 0,
    val number: Int = 0,
    val totalResults: Int = 0
)

@Serializable
data class Ingredient(
    val original: String
)

@Serializable
data class RecipeDetail(
    val id: Int,
    val title: String,
    val image: String = "",
    val readyInMinutes: Int = 0,
    val servings: Int = 0,
    val summary: String? = null,
    val instructions: String? = null,
    val extendedIngredients: List<Ingredient> = emptyList()
)