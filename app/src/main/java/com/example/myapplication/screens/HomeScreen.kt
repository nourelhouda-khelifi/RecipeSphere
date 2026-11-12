package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.model.Recipe
import com.example.myapplication.ui.theme.AppAccent
import com.example.myapplication.ui.theme.AppGray
import com.example.myapplication.ui.theme.AppTile
import com.example.myapplication.model.CUISINES
import com.example.myapplication.viewmodel.HomeUiState

// Drapeau affiché en badge (clé = valeur API de la cuisine)
private val cuisineFlags = mapOf(
    "italian" to "🇮🇹",
    "french" to "🇫🇷",
    "mexican" to "🇲🇽",
    "japanese" to "🇯🇵",
    "indian" to "🇮🇳",
    "chinese" to "🇨🇳",
    "greek" to "🇬🇷",
    "thai" to "🇹🇭",
    "spanish" to "🇪🇸",
    "middle eastern" to "🌍"
)

// Un élément qui prend toute la largeur de la grille
private val fullWidth: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

@Composable
fun HomeScreen(
    state: HomeUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onCuisineClick: (String?) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onRecipeClick: (Int) -> Unit,
    likedIds: Set<Int>,
    onToggleLike: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        // Autant de colonnes que la largeur le permet (cartes de 170 dp minimum)
        columns = GridCells.Adaptive(minSize = 170.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = fullWidth) {
            Column {
                Text(
                    text = "Recettes",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(12.dp))
                RecipeSearchBar(state.query, onQueryChange, onSearch)
                Spacer(Modifier.height(16.dp))
                CuisineRow(
                    selected = state.cuisine,
                    images = state.cuisineImages,
                    onClick = onCuisineClick
                )
                if (state.recipes.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "${state.totalResults} recettes",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppGray
                    )
                }
            }
        }

        when {
            state.isLoading -> item(span = fullWidth) { LoadingBox() }

            state.error != null && state.recipes.isEmpty() -> item(span = fullWidth) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error, color = AppGray, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = AppAccent)
                    ) { Text("Réessayer") }
                }
            }

            state.recipes.isEmpty() -> item(span = fullWidth) {
                Text(
                    "Aucune recette trouvée",
                    color = AppGray,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                items(state.recipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        liked = recipe.id in likedIds,
                        onToggleLike = { onToggleLike(recipe) },
                        onClick = { onRecipeClick(recipe.id) }
                    )
                }

                item(span = fullWidth) {
                    LoadMoreFooter(
                        shown = state.recipes.size,
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
fun LoadMoreFooter(
    shown: Int,
    total: Int,
    isLoadingMore: Boolean,
    endReached: Boolean,
    error: String?,
    onLoadMore: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$shown sur $total recettes",
            style = MaterialTheme.typography.labelMedium,
            color = AppGray
        )
        Spacer(Modifier.height(12.dp))
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        when {
            isLoadingMore -> CircularProgressIndicator(color = AppAccent)
            endReached -> Text("Vous avez tout vu", color = AppGray)
            else -> Button(
                onClick = onLoadMore,
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent)
            ) { Text("Voir plus") }
        }
    }
}

@Composable
fun LoadingBox() {
    Box(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AppAccent)
    }
}

@Composable
fun RecipeSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Rechercher une recette", color = AppGray) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AppGray) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = {
                    onQueryChange("")
                    onSearch()
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "Effacer", tint = AppGray)
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppTile,
            unfocusedContainerColor = AppTile,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = AppAccent
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CuisineRow(
    selected: String?,
    images: Map<String, String>,
    onClick: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            CuisineCircle(
                label = "Tous",
                imageUrl = null,
                icon = Icons.Filled.Apps,
                flag = null,
                selected = selected == null,
                onClick = { onClick(null) }
            )
        }
        items(CUISINES) { (value, label) ->
            CuisineCircle(
                label = label,
                imageUrl = images[value],
                icon = null,
                flag = cuisineFlags[value],
                selected = selected == value,
                onClick = { onClick(value) }
            )
        }
    }
}

@Composable
fun CuisineCircle(
    label: String,
    imageUrl: String?,
    icon: ImageVector?,
    flag: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp).clickable { onClick() }
    ) {
        Box(modifier = Modifier.size(64.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(AppTile)
                    .then(
                        if (selected) Modifier.border(2.5.dp, AppAccent, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    imageUrl != null -> AsyncImage(
                        model = imageUrl,
                        contentDescription = label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    icon != null -> Icon(icon, contentDescription = label, tint = AppAccent)
                    else -> Text(
                        text = label.take(1),
                        color = AppGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (flag != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(0.5.dp, Color.Black.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(flag, fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) AppAccent else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    liked: Boolean,
    onToggleLike: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(modifier = modifier.clickable { onClick() })  {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(AppTile)
        ) {
            AsyncImage(
                model = recipe.image,
                contentDescription = recipe.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onToggleLike,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.85f)
                ),
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(36.dp)
            ) {
                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favori",
                    tint = AppAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = recipe.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}