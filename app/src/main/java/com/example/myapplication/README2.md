# RecipeSphere

Application Android native de découverte et de gestion de recettes de cuisine, développée en **Kotlin** avec **Jetpack Compose**.



## Sommaire

- [Fonctionnalités](#fonctionnalités)

- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Structure du projet](#structure-du-projet)
- [Installation](#installation)
- [Configuration de la clé API](#configuration-de-la-clé-api)
- [Sources de données](#sources-de-données)
- [Choix techniques](#choix-techniques)
- [Limites connues et pistes d'amélioration](#limites-connues-et-pistes-damélioration)

## Fonctionnalités

- **Parcourir** une large base de recettes avec pagination.
- **Filtrer** par type de cuisine (italienne, française, mexicaine, japonaise, indienne, etc.).
- **Rechercher** une recette ou un ingrédient, avec historique des recherches récentes.
- **Consulter le détail** d'une recette : photo, durée, portions, ingrédients, étapes de préparation.
- **Créer ses propres recettes** via un formulaire (titre, image, durée, ingrédients, étapes).
- **Gérer ses recettes** : consultation et suppression avec confirmation.
- **Enregistrer des favoris**, conservés sur l'appareil après fermeture de l'application.
- **Interface adaptative** : barre de navigation en bas sur téléphone, rail latéral et affichage liste + détail côte à côte sur grand écran ou en paysage.



## Stack technique

| Domaine | Technologie |
|---|---|
| Langage | Kotlin 2.2.10 |
| Interface | Jetpack Compose, Material 3 |
| Réseau | Ktor Client (moteur CIO) |
| Navigation | Navigation Compose, Material 3 Adaptive Navigation Suite |
| SDK | minSdk 24, targetSdk 37 |

## Architecture

L'application est organisée en couches avec un flux de données unidirectionnel.

```
┌──────────────────────────────┐
│  UI (Jetpack Compose)        │  affiche l'état, transmet les actions
└──────────────┬───────────────┘
               │ StateFlow / événements
┌──────────────▼───────────────┐
│  ViewModel                   │  état de l'écran, logique de présentation
└──────────────┬───────────────┘
               │ fonctions suspend
┌──────────────▼───────────────┐
│  Repository                  │  point d'accès unique aux données
└───────┬──────────────┬───────┘
        │              │
┌───────▼──────┐ ┌─────▼────────┐
│ Ktor Client  │ │ Room         │
│ (API REST)   │ │ (SQLite)     │
└──────────────┘ └──────────────┘
```

- L'interface observe un `StateFlow` avec `collectAsStateWithLifecycle()`.
- Les actions utilisateur remontent au ViewModel, qui met à jour l'état.
- Les ViewModels survivent aux changements de configuration (rotation).

## Structure du projet

```
com.example.myapplication
├── MainActivity.kt
├── model/            Recipe, MyRecipe, Cuisines (classes @Serializable)
├── local/            FavoriteRecipe (entité), FavoriteDao, AppDatabase
├── repository/       RecipeRepository, MyRecipeRepository, FavoriteRepository
├── viewmodel/        RecipeViewModel, SearchViewModel, RecipeDetailViewModel,
│                     MyRecipesViewModel, FavoritesViewModel
├── screens/          MainScreen, HomeScreen, SearchScreen, AddRecipeScreen,
│                     MyRecipesScreen, FavoritesScreen, RecipeDetailScreen,
│                     ListDetailLayout, MyRecipeCard, PlaceholderScreen
└── ui/theme/         Couleurs et thème
```

## Installation

### Prérequis

- Android Studio (version compatible avec AGP 9.4.1)
- JDK 11 ou supérieur
- Un émulateur ou un appareil Android (API 24 minimum)
- Une clé API [Spoonacular](https://spoonacular.com/food-api) (offre gratuite disponible)

### Lancer le projet

```bash
git clone <url-du-depot>
cd MyApplication
```

1. Ouvrir le dossier dans Android Studio.
2. Renseigner la clé API (voir section suivante).
3. Lancer une synchronisation Gradle (**Sync Now**).
4. Exécuter l'application (**Run ▶**).

## Configuration de la clé API

La clé n'est jamais écrite dans le code source. Elle est lue depuis `local.properties`, fichier local non versionné.

Ajouter à la racine du projet, dans `local.properties` :

```properties
SPOONACULAR_API_KEY=votre_cle_ici
```

La clé est ensuite exposée à l'application via `BuildConfig.SPOONACULAR_API_KEY`.

Autres réglages requis (déjà présents dans le dépôt) :

- `android.permission.INTERNET` dans `AndroidManifest.xml`.
- `android.disallowKotlinSourceSets=false` dans `gradle.properties`, nécessaire pour utiliser KSP avec AGP 9.

> ⚠️ Vérifier que `local.properties` figure bien dans `.gitignore` et ne jamais publier de clé.

## Sources de données

### Spoonacular (API publique)

| Requête | Usage |
|---|---|
| `GET /recipes/complexSearch` | Accueil, recherche, filtre par cuisine, pagination |
| `GET /recipes/{id}/information` | Détail d'une recette |

L'offre gratuite est limitée par un quota journalier de points. Une erreur `402 Payment Required` indique que le quota du jour est atteint.

### MockAPI (API personnelle)

| Requête | Usage |
|---|---|
| `GET /recipes` | Liste de ses recettes |
| `POST /recipes` | Création d'une recette |
| `DELETE /recipes/{id}` | Suppression |

### Room (local)

Table `favorites` (`id`, `title`, `image`) stockant les recettes aimées. Le DAO expose un `Flow` : l'interface se met à jour automatiquement à chaque ajout ou retrait.

## Choix techniques

- **Ktor Client** dans les repositories, avec `ContentNegotiation` (JSON) et `Logging` (logcat, clé masquée).
- **`ignoreUnknownKeys = true`** : l'API renvoie de nombreux champs inutilisés par les modèles.
- **`expectSuccess = true`** : les réponses HTTP en erreur (401, 402, 429) lèvent une exception, affichée à l'utilisateur avec un bouton « Réessayer ».
- **`HttpTimeout`** de 30 secondes pour les connexions lentes.
- **Valeurs par défaut** dans les modèles `@Serializable` pour tolérer les champs absents.
- **États d'écran explicites** : chargement, erreur, liste vide, succès (`sealed interface` pour le détail).
- **Annulation des requêtes** : la recherche précédente est annulée avant d'en lancer une nouvelle, afin d'éviter l'affichage de résultats obsolètes.
- **Écran de détail unifié** : les recettes Spoonacular et MockAPI sont converties vers un modèle d'affichage commun (`RecipeDetailUi`).
- **Mise en page adaptative** : rail de navigation à partir de 600 dp, liste + détail côte à côte à partir de 840 dp, grilles à colonnes adaptatives. Le passage portrait/paysage ouvre ou ferme automatiquement la page de détail correspondante.
- **Pagination** : décalage calculé d'après le nombre d'éléments déjà chargés.


