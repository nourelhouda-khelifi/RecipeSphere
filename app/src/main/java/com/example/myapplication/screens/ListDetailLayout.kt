package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.AppBar
import com.example.myapplication.ui.theme.AppGray
import com.example.myapplication.ui.theme.AppTile

// Petit écran : seulement la liste. Grand écran : la liste à gauche, le détail à droite.
@Composable
fun ListDetailLayout(
    isExpanded: Boolean,
    list: @Composable (Modifier) -> Unit,
    detail: @Composable (Modifier) -> Unit
) {
    if (isExpanded) {
        Row(modifier = Modifier.fillMaxSize()) {
            list(Modifier.weight(1f).fillMaxHeight())
            VerticalDivider(color = AppTile)
            detail(Modifier.weight(1f).fillMaxHeight().background(AppBar))
        }
    } else {
        list(Modifier)
    }
}

// Affiché dans le panneau de droite tant qu'aucune recette n'est choisie
@Composable
fun EmptyDetail(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Choisis une recette pour voir son détail",
            color = AppGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}