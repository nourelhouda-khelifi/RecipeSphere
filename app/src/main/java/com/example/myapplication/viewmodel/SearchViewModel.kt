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

data class SearchUiState(
    val query: String = "",            // ce que l'utilisateur est en train de taper
    val searchedQuery: String = "",    // la recherche réellement envoyée à l'API
    val results: List<Recipe> = emptyList(),
    val totalResults: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null,
    val recents: List<String> = emptyList()
)

class SearchViewModel : ViewModel() {

    private val repository = RecipeRepository()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState

    private var job: Job? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun search() {
        val q = _uiState.value.query.trim()

        // Champ vide : on revient à l'écran de départ (avec les recherches récentes)
        if (q.isEmpty()) {
            job?.cancel()
            _uiState.update {
                it.copy(
                    searchedQuery = "",
                    results = emptyList(),
                    totalResults = 0,
                    isLoading = false,
                    isLoadingMore = false,
                    endReached = false,
                    hasSearched = false,
                    error = null
                )
            }
            return
        }

        // Le terme cherché passe en tête des recherches récentes (8 au maximum)
        _uiState.update {
            it.copy(
                recents = (listOf(q) + it.recents.filter { r -> !r.equals(q, ignoreCase = true) })
                    .take(8)
            )
        }
        load(query = q, reset = true)
    }

    fun searchFor(query: String) {
        _uiState.update { it.copy(query = query) }
        search()
    }

    fun clearRecents() {
        _uiState.update { it.copy(recents = emptyList()) }
    }

    fun loadNextPage() {
        val s = _uiState.value
        if (s.isLoading || s.isLoadingMore || s.endReached || !s.hasSearched) return
        load(query = s.searchedQuery, reset = false)
    }

    private fun load(query: String, reset: Boolean) {
        job?.cancel()
        val offset = if (reset) 0 else _uiState.value.results.size

        _uiState.update {
            it.copy(
                searchedQuery = query,
                hasSearched = true,
                isLoading = reset,
                isLoadingMore = !reset,
                error = null,
                results = if (reset) emptyList() else it.results,
                endReached = if (reset) false else it.endReached
            )
        }

        job = viewModelScope.launch {
            try {
                val page = repository.getRecipes(offset = offset, query = query, cuisine = null)
                val all = _uiState.value.results + page.results
                _uiState.update {
                    it.copy(
                        results = all,
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