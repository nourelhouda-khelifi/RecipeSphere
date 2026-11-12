package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import com.example.myapplication.model.MyRecipe
import com.example.myapplication.model.RecipeDetail
import com.example.myapplication.ui.theme.AppAccent
import com.example.myapplication.ui.theme.AppGray
import com.example.myapplication.ui.theme.AppIndicator
import com.example.myapplication.ui.theme.AppTile
import com.example.myapplication.viewmodel.DetailUiState

// Un seul format d'affichage, que la recette vienne de Spoonacular ou de MockAPI
data class RecipeDetailUi(
    val title: String,
    val image: String,
    val minutes: Int,
    val servings: Int,
    val summary: String,
    val ingredients: List<String>,
    val steps: List<String>
)

// Spoonacular envoie du HTML (<b>, <ol>, <li>...) : on le transforme en lignes de texte
fun htmlToLines(html: String?): List<String> {
    if (html.isNullOrBlank()) return emptyList()
    val withBreaks = html
        .replace("</li>", "\n")
        .replace("</p>", "\n")
        .replace("<br>", "\n")
        .replace("<br/>", "\n")
        .replace("<br />", "\n")
    return HtmlCompat.fromHtml(withBreaks, HtmlCompat.FROM_HTML_MODE_LEGACY)
        .toString()
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

fun RecipeDetail.toUi() = RecipeDetailUi(
    title = title,
    image = image,
    minutes = readyInMinutes,
    servings = servings,
    summary = htmlToLines(summary).joinToString(" "),
    ingredients = extendedIngredients.map { it.original },
    steps = htmlToLines(instructions)
)

// Pour MockAPI : ingrédients et étapes sont enregistrés un par ligne
fun MyRecipe.toUi() = RecipeDetailUi(
    title = title,
    image = image,
    minutes = readyInMinutes,
    servings = 0,
    summary = "",
    ingredients = ingredients.lines().map { it.trim() }.filter { it.isNotEmpty() },
    steps = instructions.lines().map { it.trim() }.filter { it.isNotEmpty() }
)

// Gère les trois états : chargement, erreur, succès
@Composable
fun RecipeDetailRoute(
    state: DetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        DetailUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppAccent)
        }

        is DetailUiState.Error -> Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(state.message, color = AppGray, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent)
            ) { Text("Réessayer") }
            TextButton(onClick = onBack) { Text("Retour", color = AppAccent) }
        }

        is DetailUiState.Success -> RecipeDetailContent(
            ui = state.recipe.toUi(),
            onBack = onBack,
            modifier = modifier
        )
    }
}

@Composable
fun RecipeDetailContent(
    ui: RecipeDetailUi,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sur grand écran, le contenu reste centré avec une largeur maximale
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Photo avec bouton retour
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(AppTile)
            ) {
                if (ui.image.isNotBlank()) {
                    AsyncImage(
                        model = ui.image,
                        contentDescription = ui.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Restaurant,
                        contentDescription = null,
                        tint = AppGray,
                        modifier = Modifier.align(Alignment.Center).size(48.dp)
                    )
                }
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = ui.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )

                val meta = listOfNotNull(
                    if (ui.minutes > 0) "${ui.minutes} min" else null,
                    if (ui.servings > 0) "${ui.servings} portions" else null
                ).joinToString("  ·  ")
                if (meta.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(meta, style = MaterialTheme.typography.labelLarge, color = AppGray)
                }

                if (ui.summary.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text(ui.summary, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(28.dp))
                DetailSectionTitle("Ingrédients")
                Spacer(Modifier.height(12.dp))
                if (ui.ingredients.isEmpty()) {
                    Text("Aucun ingrédient renseigné", color = AppGray)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ui.ingredients.forEach { IngredientRow(it) }
                    }
                }

                Spacer(Modifier.height(28.dp))
                DetailSectionTitle("Préparation")
                Spacer(Modifier.height(12.dp))
                if (ui.steps.isEmpty()) {
                    Text("Aucune étape renseignée", color = AppGray)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        ui.steps.forEachIndexed { index, step -> StepRow(index + 1, step) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSectionTitle(text: String) {
    Text(text = text, fontSize = 22.sp, fontWeight = FontWeight.Medium)
}

@Composable
private fun IngredientRow(text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(AppAccent)
        )
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StepRow(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(AppIndicator),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}