package com.example.myapplication.repository

import com.example.myapplication.local.FavoriteDao
import com.example.myapplication.local.FavoriteRecipe
import com.example.myapplication.model.Recipe
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(private val dao: FavoriteDao) {

    val favorites: Flow<List<FavoriteRecipe>> = dao.getAll()

    suspend fun add(recipe: Recipe) {
        dao.insert(FavoriteRecipe(recipe.id, recipe.title, recipe.image))
    }

    suspend fun remove(favorite: FavoriteRecipe) {
        dao.delete(favorite)
    }
}