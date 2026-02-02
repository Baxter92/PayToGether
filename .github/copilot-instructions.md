
les variables sont en français et est en camelCase
le nom de domaine pour le front est dev.dealtogether.ca
le nom de domaine pour le bff est devbff.dealtogether.ca
le nom de domaine du registry privé est registry.dealtogether.ca
Les images sont hébergées sur un registry privé artifactory et les images sont nommées comme suit:
- bff: registry.entreprise.com/bffpaytogether:latest
- front: registry.entreprise.com/frontpaytogether:latest
Pour tous les instructions prendre en compte le contexte global suivant:
L'architecture du projet est basé sur une architecture hexagonale (ou architecture en oignon) qui sépare les préoccupations en différentes couches pour améliorer la maintenabilité, la testabilité et la flexibilité du code.
le pattern de conception DDD (Domain-Driven Design) est utilisé pour structurer le code autour du domaine métier, en mettant l'accent sur la modélisation des concepts métier et en favorisant une collaboration étroite entre les développeurs et les experts métier.
Alors voici le role des différents modules dans cette architecture:
1. Module BFF-API : Ce module sert à exposer les API backend pour le module bff-front. Il agit comme une couche partie gauche du pattern hexagonal, en fournissant des points d'entrée pour les requêtes externes. Il gère la logique de présentation et la communication avec les clients front-end.
2. Module BFF-FRONT : Ce module est responsable de la gestion de l'interface utilisateur et de l'expérience utilisateur. Il est basé sur la librairie React et utilise TypeScript pour le typage statique. Ce module interagit avec le module bff-api pour récupérer les données nécessaires à l'affichage et à l'interaction avec l'utilisateur.
3. Module BFF-CORE : Ce module contient la logique métier principale de l'application. Il encapsule les règles métier, les entités et les services qui définissent le comportement de l'application. Il agit comme le cœur de l'architecture hexagonale, en fournissant une couche indépendante des détails techniques.
4. Module BFF-PROVIDER : Ce module gère les aspects techniques et les dépendances externes de l'application. Il peut inclure des intégrations avec des services tiers, des bases de données, des systèmes de messagerie, etc. Il agit comme une couche partie droite du pattern hexagonal, en fournissant les implémentations concrètes pour les ports définis dans le module bff-core.

L'ensemble de ces modules sont orcherstrer par le pom parent qui gère les dépendances et la construction du projet globalement.
chaque module a un pom.xml qui gère les dépendances spécifiques à ce module.
Lors de la rédaction de code ou de la fourniture d'instructions, il est important de respecter cette architecture et de s'assurer que chaque module interagit correctement avec les autres selon les principes de l'architecture hexagonale et du DDD.
Je veux que tu me demandes toujours que tu me demandes avant de créer une documentation des implementations ou des instructions.
le module bff-provider a un package adapter pour les implémentations des repository JPA, mapper pour mapper les fichier jpa en model core
Tous les models de tables de la base de données doivent être du suffixe jpa dans le module bff-provider. 
Les models du module bff-core doivent être indépendants de toute technologie spécifique comme JPA.
Les models du module bff-core doivent servir de pont avec l'interface provider pour servir de command et couche métier.
Les services CRUD du module bff-core doivent utiliser les models du module bff-core
le module bff-api doit utiliser un package apiadapter pour les implémentations des services CRUD et mapping des models core en dto pour l'api.
le module bff-wsclient doit contenir les services pour la génération de token JWT en utilisant WebClient pour appeler une api d'authentification externe.

le module bff-core a un package service avec les interfaces pour chaque intentions et l'implémenter dans le package impl dans le module bff-core les intentions venant des interfaces providers.
le package apiadapter dans le module bff-api doit utiliser uniquement les intentions de l'interface service du module bff-core pour implémenter les services.
écrire tous les fichiers md nécessaires pour documenter l'architecture, les modèles, les instructions d'installation et de démarrage rapide, ainsi que le résumé de l'implémentation du projet PayToGether en suivant les exemples fournis dans le dossier .github/documentation.