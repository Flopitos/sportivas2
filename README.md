
# Sportivas - Application de Prévention Santé et Motivation Sportive

## 🎯 Présentation du projet
Sportivas est une application web de prévention santé et motivation sportive destinée à être revendue à des assureurs pour réduire la sinistralité. Il s'agit d'un MVP (Minimum Viable Product) qui permet aux utilisateurs de suivre leurs activités sportives, leurs blessures, leurs performances et d'obtenir des suggestions pour améliorer leur santé.

## 🧩 Fonctionnalités

- **Authentification simple** :
  - Login avec identifiant (contrat d'assurance) + mot de passe
  - Changement obligatoire du mot de passe à la première connexion

- **Questionnaire quotidien** :
  - Enregistrement du ressenti quotidien (😊 / 😐 / 😞)

- **Gestion des sports** :
  - Ajout et suppression de sports préférés

- **Gestion des blessures** :
  - Enregistrement des blessures (zone du corps, type de douleur, date)

- **Suivi de performances** :
  - Enregistrement des performances (pas, calories, distance)
  - Calcul de points de performance (gamification)

- **Planning sportif** :
  - Ajout de séances sportives au calendrier
  - Suggestions de sports

## ⚙️ Stack technique

- Backend : Spring Boot 3.4.3
- Base de données : PostgreSQL
- Sécurité : Spring Security
- Documentation API : Swagger / OpenAPI 3
- Containerisation : Docker / Docker Compose

## 📋 Prérequis

- Java 17+
- Docker et Docker Compose
- Maven (optionnel, si vous ne voulez pas utiliser Docker)

## 🚀 Démarrage rapide

### Avec Docker Compose (recommandé)

```bash
# Cloner le dépôt
git clone <URL_DU_REPO>
cd sportivas

# Lancer l'application avec Docker Compose
docker-compose up
```

L'application sera disponible à l'adresse : http://localhost:8080

### Sans Docker (développement local)

```bash
# Cloner le dépôt
git clone <URL_DU_REPO>
cd sportivas

# Compiler l'application
./mvnw clean package

# Lancer l'application
./mvnw spring-boot:run
```

## 📥 Import des données d'activités

Pour importer les données des activités via un fichier JSON, exécutez la commande suivante :

```bash
curl -X POST -H "Content-Type: application/json" -d @activites.json http://localhost:8080/api/activities/import
```

Assurez-vous que le fichier `activites.json` est bien présent dans le répertoire courant.

## 🔑 Informations de connexion par défaut

- **Identifiant** : 123456789
- **Mot de passe** : password123

## 📚 Documentation API

La documentation Swagger/OpenAPI est disponible à l'adresse :
- http://localhost:8080/swagger-ui.html

## 📝 Endpoints REST principaux

- Auth : `/api/auth/**`
- Sports : `/api/sports/**`
- Feelings : `/api/feelings/**`
- Injuries : `/api/injuries/**`
- Performances : `/api/performances/**`
- Sessions : `/api/sessions/**`
- Activities : `/api/activities/**`