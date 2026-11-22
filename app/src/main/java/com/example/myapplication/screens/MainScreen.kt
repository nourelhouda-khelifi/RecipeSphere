package com.example.myapplication.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.navigation.AddKey
import com.example.myapplication.navigation.FavoritesKey
import com.example.myapplication.navigation.HomeKey
import com.example.myapplication.navigation.MyRecipeDetailKey
import com.example.myapplication.navigation.MyRecipesKey
import com.example.myapplication.navigation.Navigator
import com.example.myapplication.navigation.RecipeDetailKey
import com.example.myapplication.navigation.SearchKey
import com.example.myapplication.navigation.rememberNavigationState
import com.example.myapplication.navigation.toEntries
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.MyRecipesViewModel
import com.example.myapplication.viewmodel.RecipeDetailViewModel
import com.example.myapplication.viewmodel.RecipeViewModel
import com.example.myapplication.viewmodel.SearchViewModel

private data class Tab(val key: NavKey, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(HomeKey, "Accueil", Icons.Filled.Home),
    Tab(SearchKey, "Recherche", Icons.Filled.Search),
    Tab(AddKey, "Ajouter", Icons.Filled.AddCircle),
    Tab(MyRecipesKey, "Mes recettes", Icons.Filled.Book),
    Tab(FavoritesKey, "Favoris", Icons.Filled.Favorite)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: RecipeViewModel, myRecipesViewModel: MyRecipesViewModel) {
    val topLevelRoutes = remember { setOf(HomeKey, SearchKey, AddKey, MyRecipesKey, FavoritesKey) }
    val navigationState = rememberNavigationState(
        startRoute = HomeKey,
        topLevelRoutes = topLevelRoutes
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val currentTopLevelRoute = navigationState.topLevelRoute

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val myRecipesState by myRecipesViewModel.uiState.collectAsStateWithLifecycle()

    val searchViewModel: SearchViewModel = viewModel()
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val favorites by favoritesViewModel.favorites.collectAsStateWithLifecycle()
    val likedIds = favorites.map { it.id }.toSet()

    val widthDp = LocalConfiguration.current.screenWidthDp
    val useRail = widthDp >= 600
    val isExpanded = widthDp >= 840

    val titles = mapOf<NavKey, String>(
        SearchKey to "Recherche",
        AddKey to "Ajouter une recette",
        MyRecipesKey to "Mes recettes",
        FavoritesKey to "Favoris"
    )

    val entryProvider = entryProvider {
        entry<HomeKey> {
            var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
            LaunchedEffect(isExpanded) {
                val id = selectedId
                if (!isExpanded && id != null) {
                    selectedId = null
                    navigator.navigate(RecipeDetailKey(id))
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
                            else navigator.navigate(RecipeDetailKey(id))
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

        entry<SearchKey> {
            var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
            LaunchedEffect(isExpanded) {
                val id = selectedId
                if (!isExpanded && id != null) {
                    selectedId = null
                    navigator.navigate(RecipeDetailKey(id))
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
                            else navigator.navigate(RecipeDetailKey(id))
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

        entry<AddKey> {
            LaunchedEffect(Unit) { myRecipesViewModel.clearSaveError() }
            AddRecipeScreen(
                isSaving = myRecipesState.isSaving,
                error = myRecipesState.saveError,
                onSave = { recipe ->
                    myRecipesViewModel.create(recipe) {
                        navigator.navigate(MyRecipesKey)
                    }
                }
            )
        }

        entry<MyRecipesKey> {
            var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
            LaunchedEffect(isExpanded) {
                val id = selectedId
                if (!isExpanded && id != null) {
                    selectedId = null
                    navigator.navigate(MyRecipeDetailKey(id))
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
                                else navigator.navigate(MyRecipeDetailKey(id))
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

        entry<FavoritesKey> {
            var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }
            LaunchedEffect(isExpanded) {
                val id = selectedId
                if (!isExpanded && id != null) {
                    selectedId = null
                    navigator.navigate(RecipeDetailKey(id))
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
                            else navigator.navigate(RecipeDetailKey(id))
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

        entry<RecipeDetailKey> { key ->
            LaunchedEffect(isExpanded) {
                if (isExpanded) navigator.goBack()
            }
            val id = key.recipeId
            val detailViewModel: RecipeDetailViewModel = viewModel(
                key = "detail_$id",
                factory = RecipeDetailViewModel.factory(id)
            )
            val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
            RecipeDetailRoute(
                state = detailState,
                onRetry = detailViewModel::load,
                onBack = { navigator.goBack() }
            )
        }

        entry<MyRecipeDetailKey> { key ->
            LaunchedEffect(isExpanded) {
                if (isExpanded) navigator.goBack()
            }
            val id = key.id
            val recipe = myRecipesState.recipes.firstOrNull { it.id == id }
            if (recipe != null) {
                RecipeDetailContent(
                    ui = recipe.toUi(),
                    onBack = { navigator.goBack() }
                )
            } else {
                PlaceholderScreen("Recette introuvable")
            }
        }
    }

    NavigationSuiteScaffold(
        layoutType = if (useRail) NavigationSuiteType.NavigationRail
        else NavigationSuiteType.NavigationBar,
        navigationSuiteItems = {
            tabs.forEach { tab ->
                item(
                    selected = currentTopLevelRoute == tab.key,
                    onClick = { navigator.navigate(tab.key) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                titles[currentTopLevelRoute]?.let { title ->
                    CenterAlignedTopAppBar(title = { Text(title) })
                }
            }
        ) { innerPadding ->
            NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

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
