# Budgeo - Application Android

## Description
Cette application Android permet de gérer ses finances personnelles en ajoutant et suivant des transactions. Elle utilise Firebase Firestore pour stocker les transactions et affiche les trois dernières dans une interface utilisateur simple et intuitive.

## Fonctionnalités
- Ajout et suppression de transactions (recettes et dépenses)
- Affichage du solde total mis à jour dynamiquement
- Stockage des transactions dans Firebase Firestore
- Interface utilisateur avec un menu latéral de navigation
- Historique des transactions
- Visualisation des transactions sous forme de graphiques

## Technologies utilisées
- **Langage** : Kotlin
- **Base de données** : Firebase Firestore
- **UI** : RecyclerView, Navigation Drawer, Material Components

## Installation et configuration
1. **Cloner le projet**
   ```bash
   git clone https://github.com/yahmidach89/androidMoneyManager
   cd androidMoneyMangager
   ```
2. **Ouvrir le projet avec Android Studio**

4. **Lancer l'application** :
   - Assurez-vous d'avoir un émulateur ou un appareil Android connecté
   - Cliquez sur "Run" dans Android Studio

## Structure du code
- `MainActivity.kt` : Activité principale qui gère l'ajout des transactions et affiche le solde
- `TransactionAdapter.kt` : Adaptateur pour afficher la liste des transactions dans un RecyclerView
- `History.kt` : Activité affichant l'historique des transactions
- `Graphique.kt` et `GraphiqueAjout.kt` : Activités pour afficher des graphiques basés sur les transactions
- `ExpenseTracking.kt` : Activité de suivi des dépenses

## Utilisation
- **Ajouter une transaction** : Entrez un montant, une raison, un tag et cliquez sur "Ajouter" ou "Soustraire"
- **Voir l'historique** : Ouvrir le menu et sélectionner "Historique"
- **Analyser avec des graphiques** : Aller dans "Graphiques" pour une visualisation détaillée

## Auteur
Projet développé par **Yanis AHMIDACH**

## Licence
Ce projet est sous licence MIT. Vous êtes libre de l'utiliser et de le modifier selon vos besoins.

