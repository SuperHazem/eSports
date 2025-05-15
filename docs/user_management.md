# Documentation de la Gestion des Utilisateurs

## Fonctionnalités implémentées

Ce document décrit les fonctionnalités de gestion des utilisateurs implémentées dans le système, notamment les mécanismes de contrôle d'accès suivants :

### 1. Bannissement permanent

- **Description** : Permet de bloquer un utilisateur de manière permanente en cas de violations graves.
- **Fonctionnement** : L'utilisateur banni ne peut plus se connecter au système jusqu'à ce qu'un administrateur lève manuellement le bannissement.
- **Interface** : Accessible via le panneau d'administration des utilisateurs (UserAdminView).
- **Données stockées** : 
  - Statut de l'utilisateur (BANNI)
  - Raison du bannissement
  - Date de début du bannissement

### 2. Débannissement

- **Description** : Permet de réactiver l'accès d'un utilisateur précédemment banni.
- **Fonctionnement** : Restaure le statut de l'utilisateur à ACTIF, lui permettant de se reconnecter.
- **Interface** : Accessible via le panneau d'administration des utilisateurs (UserAdminView).

### 3. Suspension temporaire (Time-out)

- **Description** : Permet d'appliquer une suspension temporaire pour une durée définie.
- **Fonctionnement** : L'utilisateur ne peut pas se connecter pendant la période de suspension. L'accès est automatiquement rétabli à la fin de cette période.
- **Durées disponibles** : 24h, 48h, 3 jours, 7 jours, 14 jours, 30 jours.
- **Interface** : Accessible via le panneau d'administration des utilisateurs (UserAdminView).
- **Données stockées** : 
  - Statut de l'utilisateur (SUSPENDU)
  - Raison de la suspension
  - Date de début de la suspension
  - Date de fin de la suspension

### 4. Levée de suspension

- **Description** : Permet de lever une suspension avant sa date de fin prévue.
- **Fonctionnement** : Restaure le statut de l'utilisateur à ACTIF immédiatement.
- **Interface** : Accessible via le panneau d'administration des utilisateurs (UserAdminView).

## Modifications techniques

### Base de données

Les colonnes suivantes ont été ajoutées à la table `utilisateur` :

- `status` (VARCHAR) : Statut actuel de l'utilisateur (ACTIF, SUSPENDU, BANNI)
- `suspension_debut` (DATE) : Date de début de la suspension ou du bannissement
- `suspension_fin` (DATE) : Date de fin de la suspension (NULL pour un bannissement permanent)
- `suspension_raison` (TEXT) : Raison de la suspension ou du bannissement

### Modèle Utilisateur

Le modèle `Utilisateur` a été enrichi avec :

- Attribut `status` de type `UserStatus` (énumération)
- Attributs pour gérer les dates et raisons de suspension
- Méthodes utilitaires : `estSuspendu()`, `estBanni()`, `peutSeConnecter()`

### Contrôle d'accès

Le système vérifie automatiquement le statut de l'utilisateur lors de la connexion :

- Les utilisateurs bannis ne peuvent pas se connecter
- Les utilisateurs suspendus ne peuvent pas se connecter pendant la période de suspension
- Les suspensions expirées sont automatiquement levées lors de la tentative de connexion

## Interface d'administration

L'interface d'administration des utilisateurs permet aux administrateurs de :

- Visualiser tous les utilisateurs avec leur statut actuel
- Bannir un utilisateur avec une raison
- Débannir un utilisateur
- Suspendre temporairement un utilisateur avec une raison et une durée
- Lever une suspension avant sa date de fin

## Utilisation

Pour accéder à l'interface d'administration des utilisateurs, utilisez le bouton "Gestion des utilisateurs" dans le menu principal (accessible uniquement aux administrateurs).