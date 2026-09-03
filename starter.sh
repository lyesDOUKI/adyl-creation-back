#!/usr/bin/env bash
set -e

echo "================================================="
echo " 🚀 Démarrage de la stack Backend (Adyl Création)"
echo "================================================="
echo ""

MODE="$1"

if [ -z "$MODE" ]; then
  echo "Que veux-tu lancer ?"
  echo "  1) Infra seule (PostgreSQL + OIDC Mock) — pour lancer l'API en debug IDE"
  echo "  2) Stack complète (PostgreSQL + OIDC Mock + API en conteneur)"
  read -rp "Choix [1/2] : " CHOICE
  case "$CHOICE" in
    1) MODE="infra" ;;
    2) MODE="full" ;;
    *) echo "Choix invalide."; exit 1 ;;
  esac
fi

case "$MODE" in
  infra)
    echo "1) Lancement de l'infra (PostgreSQL, OIDC Mock)..."
    docker compose -f docker-compose.infra.yml up -d
    ;;
  full)
    echo "1) Construction de l'image de l'API..."
    docker compose -f docker-compose.infra.yml -f docker-compose.api.yml build adyl-creation-api
    echo ""
    echo "2) Lancement des services (PostgreSQL, OIDC Mock, API)..."
    docker compose -f docker-compose.infra.yml -f docker-compose.api.yml up -d
    ;;
  *)
    echo "Usage: $0 [infra|full]"
    exit 1
    ;;
esac

echo ""
echo "3) Attente de la disponibilité de la stack..."
sleep 5
echo ""
echo "================================================="
echo " ✅ Adyl Création Backend démarré avec succès ! (mode: ${MODE})"
echo "================================================="
echo " 🐘 PostgreSQL : http://localhost:${DB_PORT:-5432}"
echo " 🔐 OIDC Mock  : http://localhost:8090/adyl-creation"
if [ "$MODE" = "full" ]; then
  echo " 🚀 API Backend : http://localhost:${SERVER_PORT:-8082}"
else
  echo " 🚀 API Backend : à lancer depuis ton IDE en profil local/debug"
fi
echo "================================================="
echo " 💡 Pour obtenir un token JWT de test :"
echo "    ./get-dev-token.sh ADMIN"
echo "================================================="