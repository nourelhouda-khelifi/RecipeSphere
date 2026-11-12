package com.example.myapplication.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.viewmodel.MyRecipesViewModel
import com.example.myapplication.viewmodel.RecipeDetailViewModel
import com.example.myapplication.viewmodel.RecipeViewModel
import com.example.myapplication.viewmodel.SearchViewModel
import com.example.myapplication.viewmodel.FavoritesViewModel
private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "Accueil", Icons.Filled.Home),
    Tab("search", "Recherche", Icons.Filled.Search),
    Tab("add", "Ajouter", Icons.Filled.AddCircle),
    Tab("my_recipes", "Mes recettes", Icons.Filled.Book),
    Tab("favorites", "Favoris", Icons.Filled.Favorite)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RecipeViewModel, myRecipesViewModel: MyRecipesViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val myRecipesState by myRecipesViewModel.uiState.collectAsStateWithLifecycle()
    // Créé ici, hors du NavHost : la recherche est conservée quand on change d'onglet
    val searchViewModel: SearchViewModel = viewModel()
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val favorites by favoritesViewModel.favorites.collectAsStateWithLifecycle()
    val likedIds = favorites.map { it.id }.toSet()

    // Largeur de l'écran en dp : c'est elle qui décide de la mise en page
    val widthDp = LocalConfiguration.current.screenWidthDp
    val useRail = widthDp >= 600        // Medium et plus : rail à gauche
    val isExpanded = widthDp >= 840     // Expanded : liste + détail côte à côte

    val titles = mapOf(
        "search" to "Recherche",
        "add" to "Ajouter une recette",
        "my_recipes" to "Mes recettes",
        "favorites" to "Favoris"
    )

    NavigationSuiteScaffold(
        layoutType = if (useRail) NavigationSuiteType.NavigationRail
        else NavigationSuiteType.NavigationBar,
        navigationSuiteItems = {
            tabs.forEach { tab ->
                item(
                    selected = currentRoute == tab.route,
                    onClick = {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Pas de titre sur l'accueil ni sur le détail (ils ont leur propre en-tête)
                titles[currentRoute]?.let { title ->
                    CenterAlignedTopAppBar(title = { Text(title) })
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    // Recette affichée dans le panneau de droite (grand écran seulement)
                    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
                    // Retour en portrait avec une recette choisie : on ouvre son détail en plein écran
                    LaunchedEffect(isExpanded) {
                        val id = selectedId
                        if (!isExpanded && id != null) {
                            selectedId = null
                            navController.navigate("recipe/$id")
                        }
                    }

                    ListDetailLayout(
                        isExpanded = isExpanded,
                        list = { m ->
                            HomeScreen(
                                state = uiState,
                                onQueryChange = viewModel::onQueryChange,
                                onSearch = viewModel::search,
                                onCuisineClick = viewModel::onCuisineClick,
                                onLoadMore = viewModel::loadNextPage,
                                onRetry = viewModel::refresh,
                                onRecipeClick = { id ->
                                    if (isExpanded) selectedId = id
                                    else navController.navigate("recipe/$id")
                                },
                                likedIds = likedIds,
                                onToggleLike = favoritesViewModel::toggle,
                                modifier = m
                            )
                        },
                        detail = { m ->
                            RecipeDetailPane(
                                selectedId = selectedId,
                                onBack = { selectedId = null },
                                modifier = m
                            )
                        }
                    )
                }

                composable("search") {
                    // Recette affichée dans le panneau de droite (grand écran seulement)
                    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
                    LaunchedEffect(isExpanded) {
                        val id = selectedId
                        if (!isExpanded && id != null) {
                            selectedId = null
                            navController.navigate("recipe/$id")
                        }
                    }

                    ListDetailLayout(
                        isExpanded = isExpanded,
                        list = { m ->
                            SearchScreen(
                                state = searchState,
                                onQueryChange = searchViewModel::onQueryChange,
                                onSearch = searchViewModel::search,
                                onRecentClick = searchViewModel::searchFor,
                                onClearRecents = searchViewModel::clearRecents,
                                onLoadMore = searchViewModel::loadNextPage,
                                onRecipeClick = { id ->
                                    if (isExpanded) selectedId = id
                                    else navController.navigate("recipe/$id")
                                },
                                likedIds = likedIds,
                                onToggleLike = favoritesViewModel::toggle,
                                modifier = m
                            )
                        },
                        detail = { m ->
                            val id = selectedId
                            if (id == null) {
                                EmptyDetail(m)
                            } else {
                                val detailViewModel: RecipeDetailViewModel = viewModel(
                                    key = "detail_$id",
                                    factory = RecipeDetailViewModel.factory(id)
                                )
                                val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
                                RecipeDetailRoute(
                                    state = detailState,
                                    onRetry = detailViewModel::load,
                                    onBack = { selectedId = null },
                                    modifier = m
                                )
                            }
                        }
                    )
                }

                composable("add") {
                    LaunchedEffect(Unit) { myRecipesViewModel.clearSaveError() }
                    AddRecipeScreen(
                        isSaving = myRecipesState.isSaving,
                        error = myRecipesState.saveError,
                        onSave = { recipe ->
                            myRecipesViewModel.create(recipe) {
                                navController.navigate("my_recipes") {
                                    popUpTo("home")
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }

                composable("my_recipes") {
                    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
                    LaunchedEffect(isExpanded) {
                        val id = selectedId
                        if (!isExpanded && id != null) {
                            selectedId = null
                            navController.navigate("my_recipe/$id")
                        }
                    }

                    ListDetailLayout(
                        isExpanded = isExpanded,
                        list = { m ->
                            MyRecipesScreen(
                                state = myRecipesState,
                                onRetry = myRecipesViewModel::load,
                                onDelete = myRecipesViewModel::delete,
                                onOpen = { recipe ->
                                    recipe.id?.let { id ->
                                        if (isExpanded) selectedId = id
                                        else navController.navigate("my_recipe/$id")
                                    }
                                },
                                modifier = m
                            )
                        },
                        detail = { m ->
                            val recipe = myRecipesState.recipes.firstOrNull { it.id == selectedId }
                            if (recipe == null) {
                                EmptyDetail(m)
                            } else {
                                RecipeDetailContent(
                                    ui = recipe.toUi(),
                                    onBack = { selectedId = null },
                                    modifier = m
                                )
                            }
                        }
                    )
                }
                composable("favorites") {
                    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
                    LaunchedEffect(isExpanded) {
                        val id = selectedId
                        if (!isExpanded && id != null) {
                            selectedId = null
                            navController.navigate("recipe/$id")
                        }
                    }

                    ListDetailLayout(
                        isExpanded = isExpanded,
                        list = { m ->
                            FavoritesScreen(
                                favorites = favorites,
                                onToggleLike = favoritesViewModel::toggle,
                                onRecipeClick = { id ->
                                    if (isExpanded) selectedId = id
                                    else navController.navigate("recipe/$id")
                                },
                                modifier = m
                            )
                        },
                        detail = { m ->
                            val id = selectedId
                            if (id == null) {
                                EmptyDetail(m)
                            } else {
                                val detailViewModel: RecipeDetailViewModel = viewModel(
                                    key = "detail_$id",
                                    factory = RecipeDetailViewModel.factory(id)
                                )
                                val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
                                RecipeDetailRoute(
                                    state = detailState,
                                    onRetry = detailViewModel::load,
                                    onBack = { selectedId = null },
                                    modifier = m
                                )
                            }
                        }
                    )
                }

                // Détail plein écran d'une recette Spoonacular (petit écran)
                composable(
                    route = "recipe/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { entry ->
                    LaunchedEffect(isExpanded) {
                        if (isExpanded) navController.popBackStack()
                    }
                    val id = checkNotNull(entry.arguments?.getInt("id"))
                    val detailViewModel: RecipeDetailViewModel = viewModel(
                        key = "detail_$id",
                        factory = RecipeDetailViewModel.factory(id)
                    )
                    val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
                    RecipeDetailRoute(
                        state = detailState,
                        onRetry = detailViewModel::load,
                        onBack = { navController.popBackStack() }
                    )
                }

                // Détail plein écran d'une recette MockAPI (petit écran)
                composable(
                    route = "my_recipe/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { entry ->
                    LaunchedEffect(isExpanded) {
                        if (isExpanded) navController.popBackStack()
                    }
                    val id = entry.arguments?.getString("id")
                    val recipe = myRecipesState.recipes.firstOrNull { it.id == id }
                    if (recipe != null) {
                        RecipeDetailContent(
                            ui = recipe.toUi(),
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        PlaceholderScreen("Recette introuvable")
                    }
                }
            }
        }
    }
}
// Panneau de détail d'une recette Spoonacular (grand écran)
@Composable
private fun RecipeDetailPane(
    selectedId: Int?,
    onBack: () -> Unit,
    modifier: Modifier
) {
    if (selectedId == null) {
        EmptyDetail(modifier)
    } else {
        val detailViewModel: RecipeDetailViewModel = viewModel(
            key = "detail_$selectedId",
            factory = RecipeDetailViewModel.factory(selectedId)
        )
        val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
        RecipeDetailRoute(
            state = detailState,
            onRetry = detailViewModel::load,
            onBack = onBack,
            modifier = modifier
        )
    }
}