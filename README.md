# Adyl Creation — Backend API

Backend REST de **Adyl Creation**, développé avec **Java 25** et **Spring Boot 4.1.0**.

Le projet suit les principes **DDD** et **Architecture Hexagonale**, avec un cœur métier découplé de l'infrastructure et du fournisseur d'identité.

## 🛠️ Tech Stack

* **Java 25**
* **Spring Boot 4.1.0**
* **PostgreSQL 17**
* **Spring Security — OAuth2 Resource Server**
* **JWT**
* **Mock OAuth2 Server** pour le développement
* **Flyway** pour les migrations de base de données
* **OpenAPI + Scalar** pour la documentation API
* **Docker / Docker Compose**

## 🏗️ Architecture

L'application sépare principalement :

* **Domain** : règles métier et modèle du domaine
* **Application** : cas d'utilisation et ports
* **Infrastructure** : implémentations techniques et intégrations externes

Le domaine ne dépend pas de l'infrastructure.

L'authentification repose sur OAuth2/OIDC, mais le backend reste **agnostique du fournisseur d'identité**. Il fonctionne comme un OAuth2 Resource Server et valide les JWT reçus.

### Flux simplifié

```text
                    ┌──────────────────┐
                    │    Client / UI   │
                    └────────┬─────────┘
                             │
                             │ JWT
                             ▼
                    ┌──────────────────┐
                    │   Backend API    │
                    │                  │
                    │  Security / JWT  │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │    Application   │
                    │    Use Cases     │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │      Domain      │
                    │  Business Rules  │
                    └────────┬─────────┘
                             │
                             ▼
                    ┌──────────────────┐
                    │  Infrastructure  │
                    │ PostgreSQL / ... │
                    └──────────────────┘
```

En développement, le JWT est fourni par le **Mock OAuth2 Server**. En production, celui-ci peut être remplacé par un fournisseur OIDC réel sans modifier le cœur métier.

## 🚀 Lancement rapide

### 1. Configurer l'environnement

Copier le fichier d'environnement :

```bash
cp .env.example .env
```

Adapter les valeurs si nécessaire.

### 2. Démarrer la stack

```bash
docker compose up -d --build
```

La stack démarre :

* l'API Spring Boot ;
* PostgreSQL 17 ;
* le Mock OAuth2 Server.

Vérifier l'état des services :

```bash
docker compose ps
```

Voir les logs :

```bash
docker compose logs -f
```

## 🔐 Authentification

L'environnement de développement utilise un **Mock OAuth2 Server** afin de pouvoir tester l'authentification sans dépendre d'un fournisseur OIDC externe.

Deux rôles sont disponibles pour les tests :

* `ADMIN`
* `USER`

### Générer un token ADMIN

```bash
./scripts/get-dev-token.sh ADMIN
```

### Générer un token USER

```bash
./scripts/get-dev-token.sh USER
```

Le script génère un JWT utilisable directement pour appeler les endpoints protégés.

Exemple :

```bash
curl \
  -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8082/...
```

## 📚 Documentation API

### Scalar

http://localhost:8082/scalar/v1

Interface permettant de consulter et tester les endpoints de l'API.

### OpenAPI

http://localhost:8082/v3/api-docs

Spécification OpenAPI de l'API.

## 🗄️ Base de données

La base de données utilisée en développement est **PostgreSQL 17**.

Les évolutions du schéma sont gérées avec **Flyway** et appliquées automatiquement au démarrage de l'application.

Les migrations se trouvent dans :

```text
src/main/resources/db/migration
```

Une migration déjà exécutée ne doit pas être modifiée. Toute évolution doit faire l'objet d'une nouvelle migration.

## 🧪 Tests

Les tests couvrent notamment :

* les règles métier ;
* les cas d'utilisation ;
* les intégrations avec l'infrastructure ;
* la persistance PostgreSQL.

Les tests d'intégration utilisent PostgreSQL afin de vérifier le comportement réel des composants dépendants de la base de données.

## 🐳 Commandes utiles

### Démarrer

```bash
docker compose up -d --build
```

### Arrêter

```bash
docker compose down
```

### Voir les logs

```bash
docker compose logs -f
```

### Rebuild sans cache

```bash
docker compose build --no-cache
```

### Générer un token ADMIN

```bash
./scripts/get-dev-token.sh ADMIN
```

### Générer un token USER

```bash
./scripts/get-dev-token.sh USER
```
