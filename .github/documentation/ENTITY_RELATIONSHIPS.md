# PayToGether - Diagramme des Relations entre Entités

## Relations Principales

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         ARCHITECTURE GLOBALE                                 │
└─────────────────────────────────────────────────────────────────────────────┘

                              UTILISATEUR
                                  │
                 ┌────────────────┼────────────────┐
                 │                │                │
                 ▼                ▼                ▼
            ADRESSE      SESSION_UTILISATEUR   NOTIFICATION
           (OneToMany)       (OneToMany)       (OneToMany)
                                                    │
                 ┌──────────────┴──────────────────┤
                 │                                  │
                 ▼                                  │
              DEAL                                  │
         (créateur ManyToOne)                       │
         (participants ManyToMany)                  │
                 │                                  │
      ┌──────────┼──────────┬─────────────┐       │
      │          │          │             │        │
      ▼          ▼          ▼             ▼        │
 CATEGORIE  COMMENTAIRE  IMAGE_DEAL  POINT_FORT   │
(ManyToOne) (OneToMany)  (OneToMany) (OneToMany)  │
                 │                                  │
                 └──────────────┐                  │
                                ▼                  │
                           COMMENTAIRE             │
                          (auto-référence          │
                           pour réponses)          │
                                                    │
                 ┌──────────────┴──────────────────┤
                 │                                  │
                 ▼                                  │
            PAIEMENT ──────────────────────────────┘
        (utilisateur ManyToOne)
        (deal ManyToOne)
                 │
                 ▼
            COMMANDE
        (OneToMany paiements)
        (ManyToOne deal)
        (ManyToOne utilisateur/vendeur)
                 │
                 │
                 ▼
            PAYOUT
        (ManyToOne vendeur)
        (commission calculée)


┌─────────────────────────────────────────────────────────────────────────────┐
│                         MODULE PUBLICITÉ                                     │
└─────────────────────────────────────────────────────────────────────────────┘

            UTILISATEUR
                 │
                 ▼
            PUBLICITE
        (ManyToOne annonceur)
                 │
                 ▼
            IMAGE_PUB
           (OneToMany)


┌─────────────────────────────────────────────────────────────────────────────┐
│                    DÉTAILS DES RELATIONS PAR ENTITÉ                          │
└─────────────────────────────────────────────────────────────────────────────┘

UTILISATEUR (Hub central)
├─► Adresse (OneToMany)
├─► SessionUtilisateur (OneToMany)
├─► Notification (OneToMany)
├─► Deal.createur (OneToMany)
├─► Deal.participants (ManyToMany)
├─► Paiement (OneToMany)
├─► Commande (OneToMany vendeur)
├─► Payout (OneToMany vendeur)
├─► Publicite (OneToMany annonceur)
└─► Commentaire.auteur (OneToMany)

DEAL (Entité centrale)
├─► Utilisateur.createur (ManyToOne)
├─► Utilisateur.participants (ManyToMany)
├─► Categorie (ManyToOne)
├─► Commentaire (OneToMany)
├─► ImageDeal (OneToMany)
├─► PointFort (OneToMany)
├─► Paiement (OneToMany)
└─► Commande (OneToMany)

COMMENTAIRE (Auto-référence)
├─► Utilisateur.auteur (ManyToOne)
├─► Deal (ManyToOne)
├─► Commentaire.parent (ManyToOne)
└─► Commentaire.reponses (OneToMany)

PAIEMENT (Transaction)
├─► Utilisateur (ManyToOne)
├─► Deal (ManyToOne)
└─► Commande (ManyToOne)

COMMANDE (Agrégat)
├─► Utilisateur.vendeur (ManyToOne)
├─► Deal (ManyToOne)
└─► Paiement (OneToMany)

PAYOUT (Versement)
└─► Utilisateur.vendeur (ManyToOne)

PUBLICITE
├─► Utilisateur.annonceur (ManyToOne)
└─► ImagePub (OneToMany)


┌─────────────────────────────────────────────────────────────────────────────┐
│                    FLUX DE DONNÉES TYPIQUES                                  │
└─────────────────────────────────────────────────────────────────────────────┘

1. CRÉATION D'UN DEAL
   Utilisateur → Deal → Categorie
                  ├─► ImageDeal
                  └─► PointFort

2. PARTICIPATION À UN DEAL
   Utilisateur → Deal.participants
            └─► Paiement → Deal
                       └─► Commande

3. COMMENTAIRE SUR UN DEAL
   Utilisateur → Commentaire → Deal
                           └─► Commentaire.parent (optionnel)

4. NOTIFICATION UTILISATEUR
   Système → Notification → Utilisateur

5. SESSION UTILISATEUR
   Utilisateur → SessionUtilisateur (JWT)
            └─► JwtService.genererToken()

6. PAYOUT VENDEUR
   Vendeur → Commande → Paiement
                    └─► Payout (calcul commission)


┌─────────────────────────────────────────────────────────────────────────────┐
│                    CARDINALITÉS DES RELATIONS                                │
└─────────────────────────────────────────────────────────────────────────────┘

OneToMany (1:N)
├─► Utilisateur → Adresse
├─► Utilisateur → SessionUtilisateur
├─► Utilisateur → Notification
├─► Utilisateur → Deal (créateur)
├─► Deal → Commentaire
├─► Deal → ImageDeal
├─► Deal → PointFort
├─► Commentaire → Commentaire (réponses)
├─► Commande → Paiement
└─► Publicite → ImagePub

ManyToOne (N:1)
├─► Adresse → Utilisateur
├─► Deal → Utilisateur (créateur)
├─► Deal → Categorie
├─► Commentaire → Utilisateur (auteur)
├─► Commentaire → Deal
├─► Paiement → Utilisateur
├─► Paiement → Deal
├─► Paiement → Commande
├─► Commande → Deal
├─► Commande → Utilisateur (vendeur)
├─► Payout → Utilisateur (vendeur)
└─► Publicite → Utilisateur (annonceur)

ManyToMany (N:M)
└─► Deal ↔ Utilisateur (participants)
    Table de jointure : deal_participant


┌─────────────────────────────────────────────────────────────────────────────┐
│                    AGRÉGATS DDD                                              │
└─────────────────────────────────────────────────────────────────────────────┘

AGRÉGAT UTILISATEUR (Racine: Utilisateur)
├─► Adresse
└─► SessionUtilisateur

AGRÉGAT DEAL (Racine: Deal)
├─► ImageDeal
├─► PointFort
└─► Commentaire (partiel)

AGRÉGAT COMMANDE (Racine: Commande)
└─► Paiement

AGRÉGAT PUBLICITÉ (Racine: Publicite)
└─► ImagePub

Entités indépendantes:
├─► Categorie
├─► Role
├─► Commission
├─► Notification
└─► Payout


┌─────────────────────────────────────────────────────────────────────────────┐
│                    TABLES GÉNÉRÉES EN BASE DE DONNÉES                        │
└─────────────────────────────────────────────────────────────────────────────┘

Tables principales (16):
├─► utilisateur
├─► deal
├─► categorie
├─► commentaire
├─► paiement
├─► commande
├─► adresse
├─► notification
├─► point_fort
├─► image_deal
├─► session_utilisateur
├─► role
├─► commission
├─► payout
├─► publicite
└─► image_pub

Tables de jointure/collection:
├─► deal_participant (ManyToMany)
├─► deal_image (ElementCollection)
└─► deal_point_fort (ElementCollection)
```

## Légende

- **→** : Relation directe
- **↔** : Relation bidirectionnelle
- **OneToMany** : Un vers plusieurs
- **ManyToOne** : Plusieurs vers un
- **ManyToMany** : Plusieurs vers plusieurs
- **Racine d'agrégat** : Point d'entrée principal pour les opérations

## Notes Importantes

1. **Utilisateur** est l'entité centrale qui relie la plupart des modules
2. **Deal** est le cœur de la logique métier des offres groupées
3. **Commande** agrège les paiements pour un deal et un vendeur
4. **Commentaire** utilise une auto-référence pour les réponses imbriquées
5. Toutes les entités héritent de **BaseEntite** (UUID, dates de création/modification)
