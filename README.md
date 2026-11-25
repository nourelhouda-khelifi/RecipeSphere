# RecipeSphere

Application Android de gestion de recettes de cuisine.

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Installation](#installation)
- [Configuration de la clé API](#configuration-de-la-clé-api)


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

- Android Studio et un émulateur ou un appareil Android 
- Une clé API [Spoonacular](https://spoonacular.com/food-api) 

### Lancer le projet

```bash
git clone <url-du-depot>
cd MyApplication
```

1. Ouvrir le dossier dans Android Studio.
2. Renseigner la clé API (voir section suivante).
3. Exécuter l'application (**Run ▶**).

## Configuration de la clé API

La clé n'est pas écrite dans le code source. Elle est lue depuis `local.properties`

```properties
SPOONACULAR_API_KEY=votre_cle_ici
```

La clé est ensuite exposée à l'application via `BuildConfig.SPOONACULAR_API_KEY`.


