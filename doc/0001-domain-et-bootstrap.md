# ADR-0001 - Deux modules : `domain` et `bootstrap`

## Décision
- `domain` : l'intérieur de l'hexagone. Modèle (agrégats, entités, VO), Use Cases
  et ports, organisés par feature. Aucune dépendance technique.
- `bootstrap` : l'extérieur. API, sécurité, adapters, configuration, `main`.
- Dépendance unique : `bootstrap` → `domain`.

## Raisons
- En architecture hexagonale, « domain » désigne tout le cœur, Use Cases compris.
- Un module se justifie par sa réutilisation. Le modèle seul n'a pas d'autre
  consommateur que ses Use Cases : le séparer ajoute de l'indirection sans gain.
- `domain` complet se réutilise tel quel (API, client lourd, batch) avec un
  nouveau `bootstrap`.

## Garde-fous
- ArchUnit : `domain` ne dépend ni de Spring, ni de JPA, ni de `bootstrap`.