#!/usr/bin/env bash

set -e

echo "================================================="
echo " 🚀 Démarrage de la stack Backend (Adyl Création)"
echo "================================================="

echo ""
echo "1) Construction de l'image de l'API..."
docker compose build adyl-creation-api

echo ""
echo "2) Lancement des services (PostgreSQL, OIDC Mock, API)..."
docker compose up -d

echo ""
echo "3) Attente de la disponibilité de la stack..."
sleep 5

echo ""
echo "================================================="
echo " ✅ Adyl Création Backend démarré avec succès !"
echo "================================================="
echo " 🐘 PostgreSQL : http://localhost:${DB_PORT:-5432}"
echo " 🔐 OIDC Mock : http://localhost:8090/adyl-creation"
echo " 🚀 API Backend : http://localhost:${SERVER_PORT:-8082}"
echo "================================================="
echo " 💡 Pour obtenir un token JWT de test :"
echo "    ./get-dev-token.sh ADMIN"
echo "================================================="