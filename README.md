# RecipeSphere

Application Android native de découverte et de gestion de recettes de cuisine, développée en **Kotlin** avec **Jetpack Compose**.

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
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



## Installation

### Prérequis

- Android Studio et un émulateur ou un appareil Android (API 24 minimum)
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


## Sources de données

### Spoonacular (API publique)

| Requête | Usage |
|---|---|
| `GET /recipes/complexSearch` | Accueil, recherche, filtre par cuisine, pagination |
| `GET /recipes/{id}/information` | Détail d'une recette |

### MockAPI (API personnelle)

| Requête | Usage |
|---|---|
| `GET /recipes` | Liste de ses recettes |
| `POST /recipes` | Création d'une recette |
| `DELETE /recipes/{id}` | Suppression |

### Room (local)

Table `favorites` (`id`, `title`, `image`) stockant les recettes aimées. Le DAO expose un `Flow` : l'interface se met à jour automatiquement à chaque ajout ou retrait.



