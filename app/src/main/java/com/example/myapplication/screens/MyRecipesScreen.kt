package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.model.MyRecipe
import com.example.myapplication.ui.theme.AppAccent
import com.example.myapplication.ui.theme.AppGray
import com.example.myapplication.ui.theme.AppTile
import com.example.myapplication.viewmodel.MyRecipesUiState

@Composable
fun MyRecipesScreen(
    state: MyRecipesUiState,
    onRetry: () -> Unit,
    onDelete: (MyRecipe) -> Unit,
    onOpen: (MyRecipe) -> Unit,
    modifier: Modifier = Modifier
) {
    // La recette pour laquelle on demande confirmation de suppression
    var toDelete by remember { mutableStateOf<MyRecipe?>(null) }

    when {
        state.isLoading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppAccent)
        }

        state.error != null && state.recipes.isEmpty() -> Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(state.error, color = AppGray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent)
            ) { Text("Réessayer") }
        }

        state.recipes.isEmpty() -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Aucune recette pour l'instant.\nAjoute-en une depuis l'onglet Ajouter.",
                color = AppGray,
                textAlign = TextAlign.Center
            )
        }

        else -> LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 170.dp),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.recipes, key = { it.id ?: it.title }) { recipe ->
                MyRecipeCard(
                    recipe = recipe,
                    onClick = { onOpen(recipe) },
                    onDelete = { toDelete = recipe }
                )
            }
        }
    }

    toDelete?.let { recipe ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Supprimer cette recette ?") },
            text = { Text(recipe.title) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(recipe)
                    toDelete = null
                }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) {
                    Text("Annuler", color = AppAccent)
                }
            }
        )
    }
}

