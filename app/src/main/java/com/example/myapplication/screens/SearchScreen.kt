package com.example.myapplication.screens

import androidx.compose.foundation.clickable
import com.example.myapplication.model.Recipe
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.AppAccent
import com.example.myapplication.ui.theme.AppGray
import com.example.myapplication.viewmodel.SearchUiState

// Un élément qui prend toute la largeur de la grille
private val fullRow: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

@Composable
fun SearchScreen(
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRecentClick: (String) -> Unit,
    onClearRecents: () -> Unit,
    onLoadMore: () -> Unit,
    onRecipeClick: (Int) -> Unit,
    likedIds: Set<Int>,
    onToggleLike: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 170.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // La barre de recherche (le titre « Recherche » est déjà dans la barre du haut)
        item(span = fullRow) {
            RecipeSearchBar(state.query, onQueryChange, onSearch)
        }

        when {
            // Avant la première recherche
            !state.hasSearched -> {
                if (state.recents.isEmpty()) {
                    item(span = fullRow) { SearchHint() }
                } else {
                    item(span = fullRow) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recherches récentes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                TextButton(onClick = onClearRecents) {
                                    Text("Effacer", color = AppAccent)
                                }
                            }
                            state.recents.forEach { recent ->
                                RecentRow(text = recent, onClick = { onRecentClick(recent) })
                            }
                        }
                    }
                }
            }

            state.isLoading -> item(span = fullRow) { LoadingBox() }

            state.error != null && state.results.isEmpty() -> item(span = fullRow) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error, color = AppGray, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onSearch,
                        colors = ButtonDefaults.buttonColors(containerColor = AppAccent)
                    ) { Text("Réessayer") }
                }
            }

            state.results.isEmpty() -> item(span = fullRow) {
                Text(
                    text = "Aucun résultat pour « ${state.searchedQuery} »",
                    color = AppGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            }

            else -> {
                item(span = fullRow) {
                    Text(
                        text = "${state.totalResults} résultats pour « ${state.searchedQuery} »",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppGray
                    )
                }

                items(state.results, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        liked = recipe.id in likedIds,
                        onToggleLike = { onToggleLike(recipe) },
                        onClick = { onRecipeClick(recipe.id) }
                    )
                }

                item(span = fullRow) {
                    LoadMoreFooter(
                        shown = state.results.size,
                        total = state.totalResults,
                        isLoadingMore = state.isLoadingMore,
                        endReached = state.endReached,
                        error = state.error,
                        onLoadMore = onLoadMore
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHint() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = AppGray,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Cherche un plat ou un ingrédient\npar exemple pasta, poulet, chocolat",
            color = AppGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RecentRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.History,
            contentDescription = null,
            tint = AppGray
        )
        Spacer(Modifier.size(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}