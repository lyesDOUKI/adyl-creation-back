#!/usr/bin/env bash
# =============================================================================
# Compare le contrat OpenAPI courant avec celui de la DERNIÈRE RELEASE GitHub
# et calcule la prochaine version de l'API (SemVer strict, décidée par oasdiff).
#
# Utilisé par :
#   - release-please.yml : calcule la version et l'écrit dans la PR de release
#   - api-contract.yml   : vérifie, sur la PR de release, que la version écrite
#                          est bien celle attendue (filet de sécurité)
#
# Règles (source de vérité unique, ne pas les dupliquer ailleurs) :
#
#   Situation par rapport à la dernière release          | Version de l'API
#   -----------------------------------------------------|----------------------
#   Aucune release, ou contrat absent de cette release   | 1.0.0
#   Aucun changement (hors info.version)                 | inchangée
#   Cosmétique seul (description/summary/title/examples) | patch  : 2.3.0 -> 2.3.1
#   Fonctionnel non cassant                              | mineur : 2.3.0 -> 2.4.0
#   Fonctionnel cassant (breaking, niveau ERR)           | majeur : 2.3.0 -> 3.0.0
#
# La base de calcul est TOUJOURS le contrat de la dernière release publiée
# (jamais celui d'une PR ni de main) : aucune version n'est donc "sautée".
#
# Entrées (variables d'environnement) :
#   SPEC_PATH          (requis) chemin du contrat à évaluer, dans le dépôt courant
#   REPO               "owner/repo" (requis si LAST_TAG n'est pas fourni)
#   GH_TOKEN           token pour `gh api` (requis si LAST_TAG n'est pas fourni)
#   LAST_TAG           (optionnel) force le tag de référence (tests)
#   OUT                dossier de sortie des rapports (défaut : $RUNNER_TEMP ou /tmp)
#   OUTPUT_FILE        fichier de sortie clé=valeur (défaut : $GITHUB_OUTPUT)
#   BREAKING_LEVEL     niveau oasdiff considéré comme cassant (défaut : ERR)
#   COSMETIC_ELEMENTS  éléments considérés comme cosmétiques
#   FIRST_VERSION      version de la première API publiée (défaut : 1.0.0)
#
# Sorties (clé=valeur) :
#   state         no_spec | first | unknown | compare
#   first_reason  norelease | nocontract  (si state=first)
#   change        none | cosmetic | functional
#   breaking      true | false
#   n_err n_warn n_info
#   last_tag      tag de la dernière release
#   old_version   info.version du contrat de la dernière release
#   new_version   info.version du contrat évalué (informatif : n'entre pas dans le calcul)
#   bump          first | none | patch | minor | major
#   next_version  version à publier ("" si state = no_spec | unknown)
#
# Fichiers produits dans $OUT : diff.txt, diff.html, breaking.txt, changelog.txt,
# breaking-bullets.md, last-release-openapi.yaml.
#
# Sécurité : en cas de doute (API GitHub en erreur, tag absent, oasdiff en échec,
# version de base non conforme) on retourne state=unknown et AUCUNE version :
# on ne devine jamais une version.
# =============================================================================
set -euo pipefail

: "${SPEC_PATH:?SPEC_PATH est requis}"
OUT="${OUT:-${RUNNER_TEMP:-/tmp}}"
OUTPUT_FILE="${OUTPUT_FILE:-${GITHUB_OUTPUT:-/dev/stdout}}"
BREAKING_LEVEL="${BREAKING_LEVEL:-ERR}"
COSMETIC_ELEMENTS="${COSMETIC_ELEMENTS:-description,examples,summary,title}"
FIRST_VERSION="${FIRST_VERSION:-1.0.0}"
STABLE_RE='^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$'

mkdir -p "$OUT"
OLD_FILE="$OUT/last-release-openapi.yaml"
NORM_OLD="$OUT/last-release-openapi-normalized.yaml"
NORM_NEW="$OUT/head-openapi-normalized.yaml"

STATE="compare"; FIRST_REASON=""; CHANGE="none"; BREAKING="false"
N_ERR=0; N_WARN=0; N_INFO=0
LAST_TAG="${LAST_TAG:-}"; OLD_VERSION=""; NEW_VERSION=""
BUMP="none"; NEXT_VERSION=""

# Les sorties sont toujours publiées, même en cas de sortie anticipée.
emit_all() {
  {
    echo "state=$STATE"
    echo "first_reason=$FIRST_REASON"
    echo "change=$CHANGE"
    echo "breaking=$BREAKING"
    echo "n_err=$N_ERR"
    echo "n_warn=$N_WARN"
    echo "n_info=$N_INFO"
    echo "last_tag=$LAST_TAG"
    echo "old_version=$OLD_VERSION"
    echo "new_version=$NEW_VERSION"
    echo "bump=$BUMP"
    echo "next_version=$NEXT_VERSION"
  } >> "$OUTPUT_FILE"
}
trap emit_all EXIT

: > "$OUT/diff.txt"
: > "$OUT/changelog.txt"
: > "$OUT/breaking.txt"
: > "$OUT/breaking-bullets.md"

# --- Contrat à évaluer ------------------------------------------------------
if [[ ! -f "$SPEC_PATH" ]]; then
  echo "::notice::$SPEC_PATH est absent : rien à comparer."
  STATE="no_spec"
  exit 0
fi
NEW_VERSION="$(yq -r '.info.version // ""' "$SPEC_PATH")"
yq '.info.version = "__API_CONTRACT_VERSION__"' "$SPEC_PATH" > "$NORM_NEW"

# --- Dernière release GitHub (non brouillon, non pré-release) ---------------
# Distinguer « aucune release » (404) d'une panne : une panne ne doit JAMAIS
# être interprétée comme « première version » (on retomberait sur 1.0.0 à tort).
if [[ -z "$LAST_TAG" ]]; then
  : "${REPO:?REPO est requis pour résoudre la dernière release}"
  if RESP="$(gh api "repos/${REPO}/releases/latest" --jq '.tag_name' 2>&1)"; then
    LAST_TAG="$RESP"
  elif grep -q 'HTTP 404' <<< "$RESP"; then
    echo "ℹ️ Aucune release précédente : première version de l'API (${FIRST_VERSION})."
    STATE="first"; FIRST_REASON="norelease"
    BUMP="first"; NEXT_VERSION="$FIRST_VERSION"
    exit 0
  else
    echo "::warning::Impossible de lire la dernière release (${RESP}) : version non calculée."
    STATE="unknown"
    exit 0
  fi
fi

if ! git rev-parse -q --verify "refs/tags/${LAST_TAG}^{commit}" > /dev/null; then
  git fetch --no-tags origin "refs/tags/${LAST_TAG}:refs/tags/${LAST_TAG}" 2>/dev/null || true
fi
if ! git rev-parse -q --verify "refs/tags/${LAST_TAG}^{commit}" > /dev/null; then
  echo "::warning::Tag '${LAST_TAG}' introuvable : version non calculée."
  STATE="unknown"
  exit 0
fi

# --- Contrat de la dernière release -----------------------------------------
if ! git cat-file -e "refs/tags/${LAST_TAG}:${SPEC_PATH}" 2>/dev/null; then
  echo "ℹ️ Le contrat n'existait pas dans la release ${LAST_TAG} : première version de l'API (${FIRST_VERSION})."
  STATE="first"; FIRST_REASON="nocontract"
  BUMP="first"; NEXT_VERSION="$FIRST_VERSION"
  exit 0
fi

git show "refs/tags/${LAST_TAG}:${SPEC_PATH}" > "$OLD_FILE"
OLD_VERSION="$(yq -r '.info.version // ""' "$OLD_FILE")"
if [[ ! "$OLD_VERSION" =~ $STABLE_RE ]]; then
  echo "::warning::Version du contrat de ${LAST_TAG} non conforme ('${OLD_VERSION}', format X.Y.Z requis) : version non calculée."
  STATE="unknown"
  exit 0
fi
yq '.info.version = "__API_CONTRACT_VERSION__"' "$OLD_FILE" > "$NORM_OLD"

# --- Classification des changements (info.version exclu) ---------------------
if cmp -s "$NORM_OLD" "$NORM_NEW"; then
  echo "✅ Aucun changement d'API depuis ${LAST_TAG}."
  CHANGE="none"
else
  code=0
  oasdiff diff "$NORM_OLD" "$NORM_NEW" \
    --exclude-elements "$COSMETIC_ELEMENTS" --format text --fail-on-diff \
    > "$OUT/diff.txt" 2>&1 || code=$?

  oasdiff diff "$NORM_OLD" "$NORM_NEW" \
    --exclude-elements "$COSMETIC_ELEMENTS" --format html \
    > "$OUT/diff.html" 2>&1 || true

  if [[ $code -eq 0 ]]; then
    echo "✅ Seuls des éléments cosmétiques ($COSMETIC_ELEMENTS) ont changé depuis ${LAST_TAG}."
    CHANGE="cosmetic"
  elif [[ $code -eq 1 ]]; then
    CHANGE="functional"

    bcode=0
    oasdiff breaking "$NORM_OLD" "$NORM_NEW" \
      --fail-on "$BREAKING_LEVEL" --format text \
      > "$OUT/breaking.txt" 2>&1 || bcode=$?
    if [[ $bcode -eq 0 ]]; then
      BREAKING="false"
    elif [[ $bcode -eq 1 ]]; then
      BREAKING="true"
    else
      # Fail-safe : ne jamais retomber sur « non cassant » si l'analyse a échoué.
      echo "::warning::oasdiff breaking a échoué (code $bcode) : version non calculée."
      cat "$OUT/breaking.txt" || true
      CHANGE="none"; BREAKING="false"; STATE="unknown"
      exit 0
    fi

    oasdiff changelog "$NORM_OLD" "$NORM_NEW" \
      --format text > "$OUT/changelog.txt" 2>&1 || true

    # Une entrée oasdiff = une ligne « error|warning|info <id> at <fichier> » suivie de
    # lignes indentées (« in API GET /pets », puis le message). On regroupe chaque entrée
    # sur UNE puce lisible, classée ERR puis WARN puis INFO.
    awk '
      function flush() {
        if (cur == "") return
        if (lvl == "error")        ea[++ne] = "- ❌ **ERR** — "  cur
        else if (lvl == "warning") wa[++nw] = "- ⚠️ **WARN** — " cur
        else                       ia[++ni] = "- ℹ️ **INFO** — " cur
        cur = ""
      }
      /^(error|warning|info)[[:space:]]/ {
        flush()
        lvl = $1
        line = $0
        sub(/^(error|warning|info)[[:space:]]+/, "", line)
        cur = line
        next
      }
      /^[[:space:]]+[^[:space:]]/ && cur != "" {
        line = $0
        gsub(/^[[:space:]]+/, "", line)
        cur = cur " — " line
        next
      }
      END {
        flush()
        for (k = 1; k <= ne; k++) print ea[k]
        for (k = 1; k <= nw; k++) print wa[k]
        for (k = 1; k <= ni; k++) print ia[k]
      }
    ' "$OUT/changelog.txt" > "$OUT/breaking-bullets.md" || true

    N_ERR=$(awk  '/^error[[:space:]]/   {c++} END {print c+0}' "$OUT/changelog.txt")
    N_WARN=$(awk '/^warning[[:space:]]/ {c++} END {print c+0}' "$OUT/changelog.txt")
    N_INFO=$(awk '/^info[[:space:]]/    {c++} END {print c+0}' "$OUT/changelog.txt")

    cat "$OUT/changelog.txt"
  else
    echo "::warning::oasdiff diff a échoué (code $code) : version non calculée."
    cat "$OUT/diff.txt" || true
    STATE="unknown"
    exit 0
  fi
fi

# --- Calcul de la prochaine version, à partir de la dernière release ----------
IFS=. read -r MAJ MIN PAT <<< "$OLD_VERSION"
case "${CHANGE}:${BREAKING}" in
  none:*)
    BUMP="none";  NEXT_VERSION="${OLD_VERSION}" ;;
  cosmetic:*)
    BUMP="patch"; NEXT_VERSION="${MAJ}.${MIN}.$((PAT + 1))" ;;
  functional:false)
    BUMP="minor"; NEXT_VERSION="${MAJ}.$((MIN + 1)).0" ;;
  functional:true)
    BUMP="major"; NEXT_VERSION="$((MAJ + 1)).0.0" ;;
  *)
    echo "::error::Classification inattendue : change='${CHANGE}' breaking='${BREAKING}'."
    STATE="unknown"; BUMP="none"; NEXT_VERSION=""
    exit 0 ;;
esac

echo "🏷️ API ${OLD_VERSION} (${LAST_TAG}) → ${NEXT_VERSION} (${BUMP})"