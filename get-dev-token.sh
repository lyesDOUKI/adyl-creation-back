#!/usr/bin/env bash
set -e
ROLE=${1:-ADMIN}
MOCK_URL="http://localhost:8090/adyl-creation/token"

echo "🔑 Génération du token JWT (Rôle : $ROLE)..." >&2

RESPONSE=$(curl -s -X POST "$MOCK_URL" \
  -H "Host: oidc-mock:8090" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "grant_type=client_credentials" \
  --data-urlencode "client_id=adyl-creation-api" \
  --data-urlencode "client_secret=secret" \
  --data-urlencode "scope=openid" \
  --data-urlencode "role=$ROLE")

if command -v jq &> /dev/null; then
  TOKEN=$(echo "$RESPONSE" | jq -r '.access_token // empty')
else
  TOKEN=$(echo "$RESPONSE" | python3 -c "import sys, json; print(json.load(sys.stdin).get('access_token', ''))")
fi

if [ -z "$TOKEN" ] || [ "$TOKEN" = "None" ]; then
  echo "❌ Erreur génération token :" >&2
  echo "$RESPONSE" >&2
  exit 1
fi

echo "✅ Token généré avec succès !" >&2
echo "" >&2
echo "🔍 Contenu décodé du token (Payload) :" >&2
echo "$TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null | jq . 2>/dev/null >&2 || echo "$TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null >&2
echo "" >&2

# Seule sortie sur stdout : le header Authorization prêt à l'emploi
echo "Bearer $TOKEN"