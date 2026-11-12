package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.local.AppDatabase
import com.example.myapplication.local.FavoriteRecipe
import com.example.myapplication.model.Recipe
import com.example.myapplication.repository.FavoriteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        FavoriteRepository(AppDatabase.getInstance(application).favoriteDao())

    val favorites: StateFlow<List<FavoriteRecipe>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Si la recette est déjà en favori on la retire, sinon on l'ajoute
    fun toggle(recipe: Recipe) {
        viewModelScope.launch {
            val existing = favorites.value.firstOrNull { it.id == recipe.id }
            if (existing != null) repository.remove(existing) else repository.add(recipe)
        }
    }
}