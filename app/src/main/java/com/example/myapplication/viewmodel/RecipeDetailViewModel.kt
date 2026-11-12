package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.model.RecipeDetail
import com.example.myapplication.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    object Loading : DetailUiState
    data class Success(val recipe: RecipeDetail) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class RecipeDetailViewModel(private val recipeId: Int) : ViewModel() {

    private val repository = RecipeRepository()

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                _uiState.value = DetailUiState.Success(repository.getRecipeDetail(recipeId))
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    companion object {
        // Fabrique un ViewModel pour une recette précise
        fun factory(recipeId: Int) = viewModelFactory {
            initializer { RecipeDetailViewModel(recipeId) }
        }
    }
}