package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.RecipeViewModel
import androidx.activity.viewModels

import com.example.myapplication.screens.MainScreen
import com.example.myapplication.viewmodel.MyRecipesViewModel



class MainActivity : ComponentActivity() {
    private val viewModel: RecipeViewModel by viewModels()
    private val myRecipesViewModel: MyRecipesViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel, myRecipesViewModel)
            }
        }
    }
}

