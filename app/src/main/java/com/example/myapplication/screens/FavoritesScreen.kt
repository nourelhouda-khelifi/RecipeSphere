package com.example.myapplication.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.local.FavoriteRecipe
import com.example.myapplication.model.Recipe
import com.example.myapplication.ui.theme.AppGray

@Composable
fun FavoritesScreen(
    favorites: List<FavoriteRecipe>,
    onToggleLike: (Recipe) -> Unit,
    onRecipeClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (favorites.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Aucun favori pour l'instant.\nAppuie sur le cœur d'une recette pour l'ajouter.",
                color = AppGray,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 170.dp),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(favorites, key = { it.id }) { favorite ->
                // On reconvertit l'entité Room en Recipe pour réutiliser RecipeCard
                val recipe = Recipe(favorite.id, favorite.title, favorite.image)
                RecipeCard(
                    recipe = recipe,
                    liked = true,
                    onToggleLike = { onToggleLike(recipe) },
                    onClick = { onRecipeClick(recipe.id) }
                )
            }
        }
    }
}