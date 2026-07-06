# ShieldMind - Contrôle Parental Intelligent par IA

ShieldMind est une application Android de contrôle parental conçue pour protéger les adolescents contre les contenus toxiques (violence, haine, etc.) en utilisant une intelligence artificielle embarquée (On-Device AI) et une synchronisation en temps réel avec le parent via Firebase.

## Fonctionnalités Implémentées

1.  **Analyse en Temps Réel** : Utilise un `AccessibilityService` pour capturer le texte affiché à l'écran dans n'importe quelle application (WhatsApp, Chrome, TikTok, etc.).
2.  **Intelligence Artificielle Embarquée** : Intégration de TensorFlow Lite avec le modèle `shieldmindv2.tflite` pour classifier la toxicité du texte localement.
3.  **Interface Moderne & Animée** : Design épuré en Material 3 avec des animations fluides pour une expérience utilisateur moderne.
4.  **Tableau de Bord Parent Complet** : Suivi des alertes en temps réel, statistiques et gestion à distance des décisions de blocage.
5.  **Liaison Parent-Enfant Sécurisée** : Système d'authentification Firebase avec couplage par identifiant unique pour lier un parent à plusieurs enfants.
6.  **Sécurité Avancée** : Authentification biométrique pour les parents et protection contre la désinstallation via les privilèges d'administrateur système.

## Comment Exécuter le Projet

### 1. Prérequis
*   Android Studio Giraffe (ou plus récent).
*   Un appareil Android (ou émulateur) avec les services Google Play.
*   Un projet Firebase configuré.

### 2. Configuration de Firebase
1.  Créez un projet sur la [Console Firebase](https://console.firebase.google.com/).
2.  Ajoutez une application Android avec le package `com.example.shielmind`.
3.  Téléchargez le fichier `google-services.json` et placez-le dans le dossier `shielmind-android/app/`.
4.  Activez **Firestore Database** en mode test.
5.  Activez **Firebase Cloud Messaging**.

### 3. Compilation et Installation
1.  Ouvrez le dossier `shielmind-android` dans Android Studio.
2.  Synchronisez Gradle.
3.  Lancez l'application sur votre appareil.
4.  **Important** : Activez le service d'accessibilité "ShieldMind" dans les paramètres d'accessibilité de votre téléphone pour que l'analyse commence.

### 4. Test de Détection
L'IA détectera les contenus contenant des mots liés à la violence ou aux insultes (simulé dans `TFLiteClassifier.kt` pour la démo si le modèle nécessite une tokenisation spécifique). Lorsqu'un contenu est détecté, l'écran de blocage apparaîtra.

---
*Développé dans le cadre du projet de fin d'études ShieldMind (Juin 2026).*
