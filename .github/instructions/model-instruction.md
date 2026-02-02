Tu es un expert en developpement java sous spring boot en suivant les instructions d'architectures, je veux créer mes models, repository, crud pour :
- une entité Utilisateur avec les champs: uuid, nom, prenom, email, motDePasse, dateCreation, dateModification, statut (ACTIF, INACTIF), role (ADMIN, UTILISATEUR, VENDEUR), photoProfil (url de l'image), 
- une entité Deal avec les champs: uuid, titre, description, prixDeal, prixPart, nbParticipants, dateDebut, dateFin, statut (Brouillon, Publié), createur (relation avec Utilisateur), listeParticipants (relation avec Utilisateur), catégorie (relation avec Catégorie),liste des commentaires (relation avec Commentaire), liste des images (url des images), dateExpiration, dateCreation, dateModification, localisation (ville, pays), liste des pointsForts (liste de string)
- une entité Commentaire avec les champs: uuid, contenu, auteur (relation avec Utilisateur), deal (relation avec Deal), dateCreation, dateModification, listeReponses (relation avec Commentaire), nbLikes
- une entité Paiement avec les champs: uuid, montant, datePaiement, statut (EN_ATTENTE, CONFIRME, ECHOUE), utilisateur (relation avec Utilaisateur), deal (relation avec Deal), methodePaiement (CARTE_CREDIT, INTERAC, VIREMENT_BANCAIRE), referenceTransaction, adresseFacturation (rue, ville, codePostal, pays), dateCreation, dateModification, typePaiement (PARTICIPATION, FRAIS_SERVICE), fraisService, montantTotal, devise (CAD, USD), numeroTransaction, dateTransaction, confirmationPaiement, dateExpirationPaiement, commande(relation avec Commande)
- une entité Commande avec les champs: uuid, numeroCommande (unique), Deal (relation avec Deal), utilisateur (relation avec Utilisateur de role vendeur), listePaiements (relation avec Paiement), montantTotal, statut (EN_COURS, CONFIRMEE, ANNULEE, REMBOURSEE), dateCreation, dateModification, numeroCommande, dateExpirationCommande
- une entité Catégorie avec les champs: uuid, nom, description, dateCreation, dateModification
- une entité Adresse avec les champs: uuid, rue, ville, codePostal, pays, utilisateur (relation avec Utilisateur), dateCreation, dateModification, principale (boolean)
- une entité Notification avec les champs: uuid, typeNotification (INFO, AVERTISSEMENT, ERREUR), message, utilisateur (relation avec Utilisateur), dateCreation, dateLecture, lue (boolean), dateModification
- une entité PointFort avec les champs: uuid, description, deal (relation avec Deal), dateCreation, dateModification
- une entité ImageDeal avec les champs: uuid, urlImage, deal (relation avec Deal), dateCreation, dateModification, principale (boolean)
- une entité SessionUtilisateur avec les champs: uuid, utilisateur (relation avec Utilisateur), tokenSession, dateCreation, dateExpiration, active (boolean), adresseIP, userAgent, dateModification
- une entité Role avec les champs: uuid, nomRole (ADMIN, UTILISATEUR, VENDEUR), description, dateCreation, dateModification
- une entité Payout avec les champs: uuid, montant, numeroPayout (unique), datePayout, statut (EN_ATTENTE, TRAITE, ECHOUE), vendeur (relation avec Utilisateur de role VENDEUR), methodePayout (VIREMENT_BANCAIRE, CHEQUE), referenceTransaction, dateCreation, dateModification, numeroTransaction, dateTransaction, confirmationPayout, commissionPayout, devise (CAD, USD), montantTotal
- une entité Commission avec les champs: uuid, pourcentageCommission, montantMinimum, montant
- une entité Publicité avec les champs: uuid, titre, description, listeImagesPub(relation avec entité image pub), dateDebut, dateFin, statut (ACTIVE, INACTIVE), annonceur (relation avec Utilisateur), dateCreation, dateModification, emplacement (BANNIERE_HAUT, BANNIERE_BAS, SIDEBAR)
  - une entité ImagePub avec les champs: uuid, urlImage, publicité (relation avec Publicité), dateCreation, dateModification, principale (boolean)
  Pour chaque entité, crée les classes model, repository, et les services CRUD associés en suivant les bonnes pratiques de spring boot et de l'architecture hexagonale. Utilise les annotations JPA pour les entités et les relations entre elles. Assure toi que les repositories étendent JpaRepository et que les services contiennent les méthodes CRUD de base (create, read, update, delete).
  Les sessions utilisateur doivent être gérées de manière sécurisée en utilisant des tokens JWT pour l'authentification depuis une api d'authentification externe.
  
WebClient doit être utilisé pour les appels asynchrones vers des services externes. 
Chaque contributeur externe doit être paramétré dans les fichiers de configuration application.properties avec les URLs et les clés API nécessaires.
Les beans WebClient doivent être configurés dans une classe de configuration dédiée dans le module bff-provider. 
Je veux une classe unique permettant de récupérer les apiclients WebClient permettant une générationde tokens JWT pour l'authentification.
  Assure toi que les entités sont bien séparées en modules selon l'architecture hexagonale:
- Les models doivent être dans le module bff-core
- Les repositories doivent être dans le module bff-provider
- Les services CRUD doivent être dans le module bff-api
  Chaque module doit avoir son propre pom.xml avec les dépendances nécessaires pour spring boot, JPA, WebClient, et autres bibliothèques pertinentes.
  Fournis également des exemples de tests unitaires pour les services CRUD en utilisant JUnit et Mockito.