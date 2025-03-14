# PapiBook

PapiBook est une application mobile Android conçue pour faciliter la communication entre les seniors. Elle permet de partager des photos et de les commenter, notamment pour organiser et discuter des parties de pétanque ou d’échecs.

Ce projet a été développé dans le cadre d'un travail académique à **ISEN Yncréa Méditerranée**.

## Fonctionnalités

- Partage de publications (photos et/ou images)
- Possibilité de commenter les photos
- Like des publications partagées
- Création et édition de profils
- Recherche d’utilisateurs
- Authentification sécurisée avec Firebase

## Installation et configuration

### 1. Cloner le projet

```bash
git clone https://github.com/Nathan-Gorga/TD_Android_Social_Network.git
cd TD_Android_Social_Network
```

### 2. Ouvrir le projet dans Android Studio

1. Lancer Android Studio
2. Ouvrir le dossier `TD_Android_Social_Network`
3. Attendre l'indexation des fichiers

### 3. Installer les dépendances

Vérifiez que votre environnement est correctement configuré avec les prérequis suivants :

- **Java JDK** 17+
- **Android SDK** avec API 31+
- **Gradle** (géré automatiquement par Android Studio)
- **Google Services JSON** : Ajouter le fichier `google-services.json` dans le dossier `app/` (nécessaire pour Firebase)

Si Android Studio demande l'installation de composants manquants, acceptez-les.

### 4. Compiler et exécuter l'application

**Via Android Studio**

1. Sélectionner un émulateur ou un appareil physique
2. Lancer l'exécution avec le bouton **Run**

## Structure du projet

- `app/` : Code source principal
- `res/` : Ressources graphiques et fichiers XML
- `gradle/` : Configuration du projet

## Technologies utilisées

- **Langage** : Kotlin / Java
- **Interface utilisateur** : Jetpack Compose (ou XML UI)
- **Base de données** : Firebase Realtime Database
- **API & Réseau** : Firebase
- **Authentification** : Firebase Authentication
- **Stockage des fichiers** : Firebase Storage
- **Notifications** : Firebase Cloud Messaging

## Équipe

Ce projet a été réalisé par :

- Fatima
- Nathan
- Robin
- Thomas
- Thomas

Étudiants en **IoT Master 1** à **ISEN Yncréa Méditerranée**.
