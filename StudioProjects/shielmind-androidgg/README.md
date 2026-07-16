# ShieldMind - Contrôle Parental Intelligent par IA & Alertes Email SMTP

ShieldMind est une application Android de contrôle parental conçue pour protéger les adolescents contre les contenus inappropriés (pornographie, drogues, sexe, violence) en utilisant une intelligence artificielle embarquée (On-Device AI), un dictionnaire local intelligent et une notification automatique par email au parent.

## Architecture & Fonctionnement

1.  **Installation sur le Téléphone de l'Enfant** : Le parent installe l'application directement sur l'appareil à contrôler.
2.  **Configuration Simple & Locale** : Sur l'écran d'enregistrement, le parent saisit l'adresse email de l'enfant, celle du parent et configure les paramètres SMTP pour l'envoi de mails de manière autonome (sans serveur tiers). Aucune logique Firebase n'est requise.
3.  **Analyse Ciblée sur les Navigateurs** : Pour respecter la vie privée et optimiser les performances, l'application analyse les écrans uniquement lorsque l'enfant se trouve dans un navigateur web (Chrome, Firefox, Opera, Edge, Brave, DuckDuckGo, Samsung Internet, etc.). En dehors d'un navigateur, l'application n'analyse rien.
4.  **Fermeture Ciblée de l'Onglet** : Dès qu'un contenu inapproprié est détecté, l'application effectue immédiatement une action retour (`GLOBAL_ACTION_BACK`) pour fermer uniquement la page web incriminée, sans fermer l'application de navigation entière.
5.  **Alerte Parent Immédiate** : Un email contenant le détail du contenu bloqué et l'application source est immédiatement envoyé à l'adresse email du parent depuis l'appareil de l'enfant.

## Méthodes de Détection

L'application combine deux niveaux de détection extrêmement rapides :

*   **Dictionnaire Local Intelligent** :
    *   Mots-clés prioritaires et directs liés à la pornographie, à la drogue et au sexe.
    *   Combinaisons intelligentes de mots (détectées même sans le modèle d'IA) comme : `acheter` + `drogue`, `regarder` + `porno`, `video` + `sexe`, `deal` + `drogue`, etc.
*   **Modèle d'IA Embarquée (TensorFlow Lite)** :
    *   Classification de texte en temps réel via le modèle local `shieldmindv2.tflite` pour identifier d'autres formes de toxicité et de contenus inappropriés.

## Comment Configurer et Exécuter le Projet

### 1. Prérequis
*   Android Studio Giraffe (ou plus récent).
*   Un appareil Android (ou émulateur) avec connexion Internet.

### 2. Configuration Initiale
1.  Ouvrez le projet dans Android Studio.
2.  Synchronisez Gradle (les dépendances JavaMail `com.sun.mail` se chargeront automatiquement pour l'envoi d'emails).
3.  Compilez et lancez l'application sur le téléphone de l'enfant.

### 3. Enregistrement de l'appareil
1.  Saisissez l'email de l'enfant et l'email du parent.
2.  Entrez les paramètres du serveur SMTP d'envoi (par défaut configuré pour `smtp.gmail.com` sur le port `587`).
3.  Renseignez l'adresse de l'expéditeur et son **mot de passe d'application** (ex: mot de passe d'application généré depuis le compte Google du parent).
4.  Cliquez sur **Sauvegarder et Continuer**.

### 4. Activation des Services
*   **Service d'Accessibilité** : Activez le service d'accessibilité "ShieldMind" dans les paramètres d'accessibilité du téléphone pour lancer l'analyse en temps réel.
*   **Protection Administrateur** : Activez les droits d'administration de l'appareil pour empêcher l'enfant de désinstaller l'application sans autorisation.

---
*Développé pour garantir une protection maximale, locale, respectueuse de la vie privée et totalement autonome (Juin 2026).*
