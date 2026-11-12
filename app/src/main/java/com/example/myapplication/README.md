# Documentation technique : RecipeSphere

**Application Android de recettes de cuisine**

Document rédigé dans le cadre du projet de matiére développement mobile. Il décrit l'architecture,  les étapes de réalisation et les problèmes rencontrés.

## 1. Présentation du projet

### 1.1 Objectif

RecipeSphere est une application Android  qui permet de :

- parcourir et rechercher des recettes issues d'une API publique (Spoonacular) ;
- filtrer les recettes par type de cuisine (italienne, française, japonaise...) ;
- consulter le détail d'une recette (photo, durée, portions, ingrédients, étapes) ;
- créer, consulter et supprimer ses propres recettes grâce à une API personnelle (MockAPI) ;
- enregistrer des recettes en favoris, conservées sur l'appareil même après fermeture de l'application.


### 1.3 Périmètre fonctionnel

| Onglet | Fonction | Source des données |
| --- | --- | --- |
| Accueil | Liste paginée, recherche, filtre par cuisine | API Spoonacular |
| Recherche | Recherche libre avec historique des recherches récentes | API Spoonacular |
| Ajouter | Formulaire de création de recette | API MockAPI (POST) |
| Mes recettes | Liste et suppression de ses recettes | API MockAPI (GET, DELETE) |
| Favoris | Recettes aimées, persistées localement | Base Room |

## 2. Environnement technique

| Élément | Choix |
| --- | --- |
| Langage | Kotlin 2.2.10 |
| Navigation | Navigation Compose |
| API publique | Spoonacular  |
| API personnelle | MockAPI |

## 3. Architecture

### 3.1 Principe général

L'application suit une architecture en couches, conforme à l'architecture classique d'une application Android présentée en cours :

1. **Interface** : affiche l'état et transmet les actions de l'utilisateur.
2. **ViewModel** : stocke l'état de l'écran, décide quoi faire à chaque action, survit aux changements de configuration (rotation).
3. **Repository** : unique point d'accès aux données .
4. **Sources de données** : API distantes (Ktor) et base locale (Room).


### 3.2 Organisation du code

```
com.example.myapplication
├─ MainActivity.kt
├─ model/         Recipe, MyRecipe, Cuisines 
├─ local/         FavoriteRecipe (entité), FavoriteDao, AppDatabase (Room)
├─ repository/    RecipeRepository, MyRecipeRepository, FavoriteRepository
├─ viewmodel/     RecipeViewModel, SearchViewModel, RecipeDetailViewModel,
│                 MyRecipesViewModel, FavoritesViewModel
├─ screens/       MainScreen, HomeScreen, SearchScreen, AddRecipeScreen,
│                 MyRecipesScreen, FavoritesScreen, RecipeDetailScreen,
│                 ListDetailLayout, MyRecipeCard, PlaceholderScreen
└─ ui/theme/      Couleurs et thème
```

### 3.3 Chemin d'une donnée (exemple : l'accueil)

1. `MainActivity` crée les ViewModels avec `by viewModels()`.
2. `RecipeViewModel` lance au démarrage une coroutine (`viewModelScope.launch`) qui appelle `RecipeRepository.getRecipes(...)`.
3. Le repository envoie une requête Ktor à Spoonacular et convertit le JSON en objet `RecipeResponse`.
4. Le ViewModel met à jour son `MutableStateFlow<HomeUiState>`.
5. `MainScreen` lit cet état avec `collectAsStateWithLifecycle()` et le passe à `HomeScreen`.
6. `HomeScreen` affiche la grille. Un clic sur un élément appelle une fonction du ViewModel.

## 4. Couche données
### 4.1 Accès réseau avec Ktor

 Le client est créé dans le repository, avec deux plugins :

- `ContentNegotiation` avec `json(Json { ignoreUnknownKeys = true })` pour convertir le JSON ;
- `Logging` qui écrit les requêtes dans le logcat sous l'étiquette `Ktor-Logger`.

Paramètres supplémentaires ajoutés pour la robustesse :

- `expectSuccess = true` : toute réponse HTTP en erreur (401, 402, 429...) lève une exception, interceptée par le ViewModel et affichée à l'utilisateur ;
- `HttpTimeout` (30 secondes) en cas de connexion lente.

Chaque requête est une **fonction `suspend`**, appelée depuis une coroutine.

#### Repository Spoonacular

| Fonction | Requête | Usage |
| --- | --- | --- |
| `getRecipes(offset, query, cuisine, number)` | `GET /recipes/complexSearch` | Accueil, recherche, pagination |
| `getRecipeDetail(id)` | `GET /recipes/{id}/information` | Page de détail |

La clé API n'est pas écrite dans le code : elle est lue dans `local.properties`  et exposée via `BuildConfig.SPOONACULAR_API_KEY`. Le plugin de logging masque la valeur de la clé dans le logcat.

#### Repository MockAPI

| Fonction | Requête | Usage |
| --- | --- | --- |
| `getRecipes()` | `GET /recipes` | Liste de mes recettes |
| `createRecipe(recipe)` | `POST /recipes`  | Ajout d'une recette |
| `deleteRecipe(id)` | `DELETE /recipes/{id}` | Suppression |

### 4.2 Persistance locale avec Room

Room est structurée en trois composants

| Composant | Fichier | Rôle |
| --- | --- | --- |
| Entité | `FavoriteRecipe` | Table `favorites` (id, title, image). L'identifiant Spoonacular est la clé primaire : une recette ne peut pas être enregistrée deux fois. |
| DAO | `FavoriteDao` | `getAll()` retourne un `Flow` ; `insert()` et `delete()` sont des fonctions `suspend`. |
| Base de données | `AppDatabase` | Fournit le DAO. Le motif *singleton* (`getInstance`) garantit une seule instance de base dans l'application. |

Le DAO retourne un `Flow` : lorsqu'une ligne est ajoutée ou supprimée, la liste observée est mise à jour automatiquement, sans rechargement manuel.

`FavoriteRepository` expose ce flux et deux opérations (`add`, `remove`). Il convertit une `Recipe` en entité `FavoriteRecipe`.

## 5. Gestion d'état : les ViewModels

Chaque écran possède un état décrit par une classe de données, exposé par un `StateFlow` en lecture seule (le `MutableStateFlow` reste privé). Cette séparation garantit que seul le ViewModel modifie l'état.

| ViewModel | État | Responsabilité |
| --- | --- | --- |
| `RecipeViewModel` | `HomeUiState` | Liste de l'accueil, filtre de cuisine, recherche, pagination |
| `SearchViewModel` | `SearchUiState` | Recherche libre, historique (8 entrées maximum), pagination |
| `RecipeDetailViewModel` | `DetailUiState` (sealed : `Loading`, `Success`, `Error`) | Détail d'une recette ; créé avec une fabrique recevant l'identifiant |
| `MyRecipesViewModel` | `MyRecipesUiState` | Lecture, création, suppression des recettes personnelles |
| `FavoritesViewModel` | `StateFlow<List<FavoriteRecipe>>` | Favoris issus de Room, bascule ajout/retrait |

## 6. Interface utilisateur (Jetpack Compose)

### 6.1 Principes

Chaque élément visuel est une fonction `@Composable` qui reçoit des paramètres. L'état est **remonté** (*state hoisting*) : les composants de présentation ne gardent pas d'état métier, ils reçoivent des valeurs et des fonctions de rappel (`onClick`, `onToggleLike`, `onSave`...). Cela les rend réutilisables : `RecipeCard` sert dans l'accueil, la recherche et les favoris.



### 6.2 Gestion des états d'un écran

Chaque écran traite explicitement les cas : chargement (indicateur), erreur (message et bouton « Réessayer »), liste vide (message explicatif) et succès. Le détail utilise un `sealed interface` (`Loading`, `Success`, `Error`) évalué par un `when` exhaustif.

### 6.3 Écran de détail unifié

Une recette Spoonacular et une recette MockAPI ont des formats différents. Les deux sont converties en un modèle d'affichage unique, `RecipeDetailUi`, par deux fonctions d'extension (`RecipeDetail.toUi()` et `MyRecipe.toUi()`). Un seul composant (`RecipeDetailContent`) affiche donc les deux types. Le HTML renvoyé par Spoonacular est converti en lignes de texte par `htmlToLines`.

### 6.4 Formulaire d'ajout

`AddRecipeScreen` gère localement le titre, l'image, la durée, ainsi que des listes dynamiques d'ingrédients et d'étapes (`mutableStateListOf`). Le titre est obligatoire. Une saisie non validée par le bouton « + » est tout de même prise en compte à l'enregistrement. L'action remonte par `onSave`, et le ViewModel gère l'envoi (état `isSaving`, message `saveError`).

## 7. Navigation et adaptation à la taille d'écran

### 7.1 Navigation

L'écran principal (`MainScreen`) combine :

- `NavigationSuiteScaffold` : barre de navigation en bas sur petit écran, rail latéral sur écran large ;
- `NavHost` avec les routes `home`, `search`, `add`, `my_recipes`, `favorites`, `recipe/{id}` et `my_recipe/{id}` ;
- une navigation d'onglets qui sauvegarde et restaure l'état (`saveState`, `restoreState`, `launchSingleTop`).


### 7.2 Layouts adaptatifs

 Les seuils utilisés suivent les classes de largeur d'Android :

| Largeur | Mise en page |
| --- | --- |
| moins de 600 dp | Barre de navigation en bas, liste seule, détail en plein écran |
| 600 dp ou plus | Rail de navigation à gauche |
| 840 dp ou plus | Liste et détail côte à côte (`ListDetailLayout`) |

Autres adaptations : grilles à colonnes adaptatives (`GridCells.Adaptive(170.dp)`) et formulaire d'ajout sur deux colonnes en grand écran (`BoxWithConstraints`).

### 7.3 Changement d'orientation

Le détail existe sous deux formes : un panneau (grand écran) et une page (petit écran). Deux mécanismes `LaunchedEffect(isExpanded)` assurent la cohérence :

- **Paysage vers portrait** : si une recette est sélectionnée dans le panneau, la sélection est effacée et la page `recipe/{id}` (ou `my_recipe/{id}`) s'ouvre en plein écran ;
- **Portrait vers paysage** : si une page de détail est ouverte, elle se ferme (`popBackStack`) pour revenir à la mise en page liste et détail.

La sélection n'est pas transférée à l'écran retrouvé dans le second cas ; le panneau affiche « Choisis une recette ».




