package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Recipe
import com.example.myapplication.repository.RecipeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Valeurs acceptées par le paramètre "cuisine" de Spoonacular (valeur API, libellé affiché)


data class HomeUiState(
    val recipes: List<Recipe> = emptyList(),
    val totalResults: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val cuisine: String? = null,
    val cuisineImages: Map<String, String> = emptyMap()
)

class RecipeViewModel : ViewModel() {

    private val repository = RecipeRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var loadJob: Job? = null

    init {
        refresh()
    }

    fun refresh() = load(reset = true)

    fun search() = load(reset = true)

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    // cuisine = null signifie "Tous" (aucun filtre)
    fun onCuisineClick(cuisine: String?) {
        _uiState.update { it.copy(cuisine = cuisine) }
        load(reset = true)
    }

    fun loadNextPage() {
        val s = _uiState.value
        if (s.isLoading || s.isLoadingMore || s.endReached) return
        load(reset = false)
    }



    private fun load(reset: Boolean) {
        loadJob?.cancel()
        val s = _uiState.value
        val offset = if (reset) 0 else s.recipes.size

        _uiState.update {
            it.copy(
                isLoading = reset,
                isLoadingMore = !reset,
                error = null,
                recipes = if (reset) emptyList() else it.recipes,
                endReached = if (reset) false else it.endReached
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val page = repository.getRecipes(
                    offset = offset,
                    query = s.query.ifBlank { null },
                    cuisine = s.cuisine
                )
                val all = _uiState.value.recipes + page.results
                _uiState.update {
                    it.copy(
                        recipes = all,
                        totalResults = page.totalResults,
                        isLoading = false,
                        isLoadingMore = false,
                        endReached = page.results.isEmpty() || all.size >= page.totalResults
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = e.message ?: "Erreur inconnue"
                    )
                }
            }
        }
    }
}