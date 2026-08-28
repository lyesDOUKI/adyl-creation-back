## ⚡ Optimisation des tests d'intégration locaux (Testcontainers)

Pour éviter de détruire et redémarrer le conteneur PostgreSQL à chaque exécution de test en dev local, le projet utilise la fonctionnalité Reuse de Testcontainers.
Configuration initiale (à faire une seule fois)

Pour activer l'option sur la machine :
```bash
echo "testcontainers.reuse.enable=true" >> ~/.testcontainers.properties
``` 
Pourquoi ce n'est pas activé par défaut dans le dépôt ?

- Isolation CI/CD : Cette configuration réside dans le répertoire utilisateur (~/.testcontainers.properties) et ne touche pas au code source. Les pipelines de CI/CD continueront de démarrer des conteneurs 100 % isolés à chaque build.
- Gain de temps : Le conteneur reste actif en arrière-plan sur ta machine, évitant le temps de démarrage (3 à 5 secondes) à chaque lancement de mvn test.