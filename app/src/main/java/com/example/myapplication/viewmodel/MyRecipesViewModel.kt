package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.MyRecipe
import com.example.myapplication.repository.MyRecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyRecipesUiState(
    val recipes: List<MyRecipe> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveError: String? = null
)

class MyRecipesViewModel : ViewModel() {

    private val repository = MyRecipeRepository()

    private val _uiState = MutableStateFlow(MyRecipesUiState())
    val uiState: StateFlow<MyRecipesUiState> = _uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val list = repository.getRecipes()
                _uiState.update { it.copy(recipes = list, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Erreur inconnue")
                }
            }
        }
    }

    fun create(recipe: MyRecipe, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                val created = repository.createRecipe(recipe)
                // La nouvelle recette apparaît en haut de la liste
                _uiState.update {
                    it.copy(recipes = listOf(created) + it.recipes, isSaving = false)
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, saveError = e.message ?: "Erreur inconnue")
                }
            }
        }
    }

    fun clearSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun delete(recipe: MyRecipe) {
        val id = recipe.id ?: return
        viewModelScope.launch {
            try {
                repository.deleteRecipe(id)
                _uiState.update { state ->
                    state.copy(recipes = state.recipes.filter { it.id != id })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Erreur inconnue") }
            }
        }
    }
}