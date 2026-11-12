# ADR-0002 - Ports orientés intentions

## Décision
Pas de Repository générique par agrégat. Chaque port exprime un besoin d'un Use
Case : `OrderCreator`, `CustomerOrderHistoryFinder`, `DiscountClaimer`,
`OrderEditor`.

`OrderEditor` regroupe bien `findById` et `save`, car les deux relèvent du même cycle
de vie d'un agrégat. Les lectures (écrans, listes, rapports) passent par des
ports dédiés qui retournent des projections.

## Raisons
- Interfaces étroites : un Use Case ne dépend que de ce dont il a besoin.
- Pas de CRUD générique ni de méthodes `findByXAndYAndZ` accumulées.
- Les ports vivent dans `domain`, à côté des Use Cases qui les utilisent.

## Règles
- Un port par besoin cohérent, jamais par méthode technique.
- Nom explicite sur l'intention, rangé dans le package de sa feature.