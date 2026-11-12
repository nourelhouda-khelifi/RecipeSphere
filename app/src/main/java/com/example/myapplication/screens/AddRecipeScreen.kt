package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.model.MyRecipe
import com.example.myapplication.ui.theme.AppAccent
import com.example.myapplication.ui.theme.AppGray
import com.example.myapplication.ui.theme.AppIndicator
import com.example.myapplication.ui.theme.AppTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    isSaving: Boolean,
    error: String?,
    onSave: (MyRecipe) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var image by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf(0) }
    var titleError by remember { mutableStateOf(false) }

    var ingredientInput by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<String>() }

    var stepInput by remember { mutableStateOf("") }
    val steps = remember { mutableStateListOf<String>() }

    // Bloc 1 : photo, titre, lien de l'image, temps
    val basicsSection: @Composable (Float) -> Unit = { ratio ->
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ImagePreview(image = image, ratio = ratio)

            AppTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = "Titre de la recette *",
                errorText = if (titleError) "Le titre est obligatoire" else null
            )

            AppTextField(
                value = image,
                onValueChange = { image = it },
                label = "Lien de l'image (facultatif)"
            )

            SectionLabel("Temps de préparation")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 30, 45, 60, 90).forEach { value ->
                    FilterChip(
                        selected = minutes == value,
                        onClick = { minutes = if (minutes == value) 0 else value },
                        label = { Text("$value min") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppIndicator,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }
    }

    // Bloc 2 : ingrédients et étapes
    val listsSection: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionLabel("Ingrédients")
            AddItemField(
                value = ingredientInput,
                onValueChange = { ingredientInput = it },
                label = "Ajouter un ingrédient",
                onAdd = {
                    if (ingredientInput.isNotBlank()) {
                        ingredients.add(ingredientInput.trim())
                        ingredientInput = ""
                    }
                }
            )
            ingredients.forEachIndexed { index, item ->
                ItemRow(text = "•  $item", onDelete = { ingredients.removeAt(index) })
            }

            SectionLabel("Étapes")
            AddItemField(
                value = stepInput,
                onValueChange = { stepInput = it },
                label = "Ajouter une étape",
                onAdd = {
                    if (stepInput.isNotBlank()) {
                        steps.add(stepInput.trim())
                        stepInput = ""
                    }
                }
            )
            steps.forEachIndexed { index, item ->
                ItemRow(text = "${index + 1}.  $item", onDelete = { steps.removeAt(index) })
            }
        }
    }

    // Bloc 3 : message d'erreur et bouton Enregistrer
    val saveSection: @Composable () -> Unit = {
        Column {
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        // On n'oublie pas un texte tapé mais pas encore ajouté avec le +
                        val finalIngredients = (ingredients + ingredientInput.trim())
                            .filter { it.isNotBlank() }
                        val finalSteps = (steps + stepInput.trim())
                            .filter { it.isNotBlank() }
                        onSave(
                            MyRecipe(
                                title = title.trim(),
                                image = image.trim(),
                                ingredients = finalIngredients.joinToString("\n"),
                                instructions = finalSteps.joinToString("\n"),
                                readyInMinutes = minutes
                            )
                        )
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enregistrer la recette", fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    // La mise en page dépend de la largeur réellement disponible
    BoxWithConstraints(modifier = modifier.fillMaxSize().imePadding()) {
        val twoColumns = maxWidth >= 600.dp

        if (twoColumns) {
            // Paysage / grand écran : deux colonnes qui défilent séparément
            Row(
                modifier = Modifier
                    .widthIn(max = 1000.dp)
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    basicsSection(2f)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    listsSection()
                    saveSection()
                }
            }
        } else {
            // Portrait : une colonne, bouton fixé en bas
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
                    .align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    basicsSection(16f / 9f)
                    listsSection()
                }
                Box(modifier = Modifier.padding(16.dp)) {
                    saveSection()
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = AppTile,
    unfocusedContainerColor = AppTile,
    errorContainerColor = AppTile,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
    focusedLabelColor = AppAccent,
    cursorColor = AppAccent
)

@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorText: String? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = errorText != null,
        supportingText = if (errorText != null) {
            { Text(errorText) }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = fieldColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
    )
}

// Aperçu de la photo : un rectangle gris clair, comme les cartes de l'accueil
@Composable
private fun ImagePreview(image: String, ratio: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(16.dp))
            .background(AppTile),
        contentAlignment = Alignment.Center
    ) {
        if (image.isNotBlank()) {
            AsyncImage(
                model = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint = AppGray,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Aperçu de la photo",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppGray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AddItemField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    onAdd: () -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = fieldColors(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onAdd() }),
        trailingIcon = {
            IconButton(
                onClick = onAdd,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = AppAccent,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ItemRow(text: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTile)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Close, contentDescription = "Retirer", tint = AppGray)
        }
    }
}