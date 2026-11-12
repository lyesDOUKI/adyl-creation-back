# ADR-0003 - Persistance par Snapshot

## Décision
L'infrastructure ne construit ni ne retourne jamais un agrégat : elle manipule
des Snapshots (records représentant l'état persisté).

- Chargement : le port retourne un Snapshot, le Use Case reconstitue l'agrégat
  via un mécanisme de `restore`.
- Sauvegarde : l'agrégat produit un Snapshot, transmis au port.
- Création et reconstitution sont deux opérations distinctes.
- La reconstitution ne rejoue pas les règles de création (elles évoluent) :
  seuls les invariants structurels sont vérifiés.

## Raisons
- Domaine sans annotation JPA ni objet géré par un ORM.
- Changer de technologie de persistance n'impacte que les adapters.
- Use Cases testables avec des fakes en mémoire.

## Coûts
- Trois représentations (entité de persistance, Snapshot, agrégat) et deux
  mappings à maintenir.