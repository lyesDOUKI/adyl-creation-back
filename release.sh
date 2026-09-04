#!/usr/bin/env bash
set -euo pipefail

# Usage: ./release.sh <patch|minor|major>
BUMP_TYPE="${1:?Usage: ./release.sh <patch|minor|major>}"

if [[ "$BUMP_TYPE" != "patch" && "$BUMP_TYPE" != "minor" && "$BUMP_TYPE" != "major" ]]; then
    echo "Erreur : le type doit être patch, minor ou major"
    exit 1
fi

# --- 1. Lecture de la version actuelle depuis le pom racine ---
CURRENT_VERSION=$(mvn -q -pl . -Dexpression=project.version -DforceStdout help:evaluate)
echo "Version actuelle : $CURRENT_VERSION"

if [[ ! "$CURRENT_VERSION" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)-SNAPSHOT$ ]]; then
    echo "Erreur : la version courante doit être au format X.Y.Z-SNAPSHOT (trouvé: $CURRENT_VERSION)"
    exit 1
fi

MAJOR="${BASH_REMATCH[1]}"
MINOR="${BASH_REMATCH[2]}"
PATCH="${BASH_REMATCH[3]}"

# --- 2. Calcul de la version de release selon le type de bump ---
case "$BUMP_TYPE" in
    patch)
        RELEASE_VERSION="${MAJOR}.${MINOR}.$((PATCH + 1))"
        ;;
    minor)
        RELEASE_VERSION="${MAJOR}.$((MINOR + 1)).0"
        ;;
    major)
        RELEASE_VERSION="$((MAJOR + 1)).0.0"
        ;;
esac

NEXT_SNAPSHOT="${RELEASE_VERSION%.*}.$(( ${RELEASE_VERSION##*.} + 1 ))-SNAPSHOT"

echo "== Release à créer : $RELEASE_VERSION =="
echo "== Prochaine version de dev : $NEXT_SNAPSHOT =="
read -rp "Confirmer ? (y/N) " CONFIRM
[[ "$CONFIRM" == "y" ]] || { echo "Annulé."; exit 1; }

# --- 3. Vérifier que le working tree est propre ---
if [[ -n "$(git status --porcelain)" ]]; then
    echo "Erreur : des changements non commités sont présents. Committez ou stashez d'abord."
    exit 1
fi

# --- 4. Passage en version de release sur tout le reactor ---
mvn versions:set -DnewVersion="$RELEASE_VERSION" -DprocessAllModules=true -DgenerateBackupPoms=false

# --- 5. Build complet + tests avant de figer quoi que ce soit ---
mvn clean install

# --- 6. Commit + tag de la release ---
git add -A
git commit -m "release: version $RELEASE_VERSION"
git tag -a "v$RELEASE_VERSION" -m "Release $RELEASE_VERSION"

# --- 7. Retour en SNAPSHOT pour continuer le développement ---
mvn versions:set -DnewVersion="$NEXT_SNAPSHOT" -DprocessAllModules=true -DgenerateBackupPoms=false
git add -A
git commit -m "chore: prepare next development version $NEXT_SNAPSHOT"

# --- 8. Push (commits + tag) ---
git push origin HEAD
git push origin "v$RELEASE_VERSION"

echo "Release $RELEASE_VERSION terminée. Développement repris sur $NEXT_SNAPSHOT."