Tu peux me générer mes objects kubernetes permettant de déployer les images bff et front de mon projet basé sur leurs dockerfiles et stp check le docker file du front pour corriger en cas d'erreur fichier deployement et exposé en externe via ingress sur un nom de domaine pour le front et pour le backend aussi stp comme suit:
- front: front.paytogether.entreprise.com
- bff: api.paytogether.entreprise.com

Les déploiements doivent inclure les ressources nécessaires (CPU et mémoire) et les probes de liveness et readiness.  
Les services doivent être de type loadBalancer pour le front et clusterIP pour le bff.
Les ingress doivent être configurés pour rediriger le trafic vers les services appropriés en fonction des noms de domaine spécifiés.
Les variables sont en français et en camelCase.
Les images sont hébergées sur un registry privé artifactory et les images sont nommées comme suit:
- bff: registry.entreprise.com/bffpaytogether:latest
- front: registry.entreprise.com/frontpaytogether:latest