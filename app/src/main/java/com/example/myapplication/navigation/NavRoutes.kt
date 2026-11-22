package com.example.myapplication.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

@Serializable
data object SearchKey : NavKey

@Serializable
data object AddKey : NavKey

@Serializable
data object MyRecipesKey : NavKey

@Serializable
data object FavoritesKey : NavKey

@Serializable
data class RecipeDetailKey(val recipeId: Int) : NavKey

@Serializable
data class MyRecipeDetailKey(val id: String) : NavKey
