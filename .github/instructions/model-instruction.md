# Instructions - Création des Modèles et CRUD

## 🎯 Objectif

Créer les modèles, repositories, services et API pour toutes les entités du projet PayToGether en suivant l'architecture hexagonale et les conventions établies.

---

## 📦 Entités à créer

### 1. **Utilisateur**
**Champs** :
- `uuid` : UUID (PK)
- `nom` : String (max 100 caractères)
- `prenom` : String (max 100 caractères)
- `email` : String (unique, max 255 caractères, obligatoire)
- `motDePasse` : String (haché avec BCrypt, obligatoire)
- `statut` : Enum (ACTIF, INACTIF) - défaut: INACTIF
- `role` : Enum (ADMIN, UTILISATEUR, VENDEUR) - défaut: UTILISATEUR
- `photoProfil` : Relation OneToOne avec ImageUtilisateur
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- **OneToOne** avec **ImageUtilisateur** (photo de profil)
  - Nom du champ JPA : `photoProfil`
  - Un utilisateur a une seule photo de profil
  
- **OneToMany** avec **Deal** (en tant que créateur/marchand)
  - Inverse de `DealJpa.marchandJpa`
  - Un utilisateur peut créer plusieurs deals
  - **Note** : Cette relation inverse n'est généralement pas mappée dans `UtilisateurJpa` pour éviter les performances
  - Pour obtenir les deals d'un utilisateur : `DealRepository.findByMarchandJpaUuid(UUID utilisateurUuid)`
  
- **ManyToMany** avec **Deal** (en tant que participant)
  - Inverse de `DealJpa.participants`
  - Table de jointure : `deal_participants`
  - Un utilisateur peut participer à plusieurs deals
  - **Note** : Cette relation inverse n'est généralement pas mappée dans `UtilisateurJpa`
  - Pour obtenir les participations : `DealRepository.findByParticipantsUuid(UUID utilisateurUuid)`
  
- **OneToMany** avec **Adresse**
- **OneToMany** avec **Paiement**
- **OneToMany** avec **Commande**
- **OneToMany** avec **Notification**
- **OneToMany** avec **Commentaire** (auteur)

**Gestion images** :
- Photo de profil unique avec statut (PENDING, UPLOADED, FAILED)
- Génération URL présignée pour upload et lecture
- Méthode utilitaire : `setPhotoProfilUnique(String folder, String urlImage)` pour ajouter timestamp

**Sécurité** :
- Le mot de passe est haché avec **BCrypt** (10 rounds par défaut)
- Ne jamais retourner le mot de passe haché dans les DTOs de réponse
- Utiliser `BCryptPasswordEncoder.encode()` pour hasher
- Utiliser `BCryptPasswordEncoder.matches()` pour vérifier

**Important** :
- Les relations inverses (OneToMany depuis Utilisateur vers Deal) ne sont pas mappées dans l'entité JPA pour des raisons de performance
- Utiliser des requêtes spécifiques dans les repositories pour obtenir ces relations

---

### 2. **Deal**
**Champs** :
- `uuid` : UUID (PK)
- `titre` : String
- `description` : String (TEXT)
- `prixDeal` : BigDecimal
- `prixPart` : BigDecimal
- `nbParticipants` : Integer
- `dateDebut` : LocalDateTime
- `dateFin` : LocalDateTime
- `dateExpiration` : LocalDateTime
- `statut` : Enum (BROUILLON, PUBLIE, TERMINE, ANNULE)
- `ville` : String
- `pays` : String
- `listePointsForts` : List<String> (@ElementCollection)
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- **ManyToOne** avec **Utilisateur** (créateur/marchand) - `@JoinColumn(name = "utilisateur_uuid", nullable = false)`
  - Nom du champ JPA : `marchandJpa` (ou `createurJpa`)
  - Nom du champ Modèle : `createur`
  - Un utilisateur peut créer plusieurs deals
  - Un deal a un seul créateur
  
- **ManyToOne** avec **Catégorie** - `@JoinColumn(name = "categorie_uuid", nullable = false)`
  - Nom du champ JPA : `categorieJpa`
  - Nom du champ Modèle : `categorie`
  - Une catégorie peut contenir plusieurs deals
  - Un deal appartient à une seule catégorie
  
- **ManyToMany** avec **Utilisateur** (participants) - Table de jointure `deal_participants`
  - `@JoinTable(name = "deal_participants", joinColumns = @JoinColumn(name = "deal_uuid"), inverseJoinColumns = @JoinColumn(name = "utilisateur_uuid"))`
  - Nom du champ JPA : `participants` (Set<UtilisateurJpa>)
  - Un deal peut avoir plusieurs participants
  - Un utilisateur peut participer à plusieurs deals
  - **Note** : Cette relation n'est pas présente dans le modèle métier (DealModele) - gérée uniquement au niveau JPA
  
- **OneToMany** avec **ImageDeal** - `mappedBy = "dealJpa"`
  - Cascade ALL, orphanRemoval = true
  - Un deal peut avoir plusieurs images
  - Une image appartient à un seul deal
  
- **OneToMany** avec **Commentaire** (à implémenter)
- **OneToMany** avec **Paiement** via **Commande** (à implémenter)

**Gestion images** :
- Liste d'images avec une principale (`isPrincipal`)
- Statut pour chaque image (PENDING, UPLOADED, FAILED)
- Génération URL présignées pour upload et lecture

**Important** :
- Dans le **modèle métier** (`DealModele`) : utiliser `createur` de type `UtilisateurModele`
- Dans l'**entité JPA** (`DealJpa`) : utiliser `marchandJpa` de type `UtilisateurJpa`
- Le mapper JPA doit faire la conversion : `marchandJpa` ↔ `createur`

---

### 3. **Catégorie**
**Champs** :
- `uuid` : UUID (PK)
- `nom` : String (unique, max 100 caractères)
- `description` : String (TEXT)
- `icone` : String (URL ou nom fichier, max 255 caractères)
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- **OneToMany** avec **Deal** - `mappedBy = "categorieJpa"`
  - Nom du champ JPA : `deals` (List<DealJpa>)
  - Cascade ALL, orphanRemoval = true, FetchType.LAZY
  - Une catégorie peut contenir plusieurs deals
  - Lorsqu'une catégorie est supprimée, tous ses deals sont également supprimés (cascade)
  - **Note** : Cette relation inverse n'est généralement pas présente dans le modèle métier (`CategorieModele`) pour éviter les dépendances circulaires

**Contraintes** :
- `nom` : unique et obligatoire
- Index recommandé sur `nom` pour les recherches

**Important** :
- La relation bidirectionnelle est gérée uniquement au niveau JPA
- Le modèle métier `CategorieModele` ne contient pas la liste des deals
- Pour obtenir les deals d'une catégorie, utiliser `DealRepository.findByCategorieUuid(UUID categorieUuid)`

---

### 4. **Commentaire**
**Champs** :
- `uuid` : UUID (PK)
- `contenu` : String (TEXT)
- `nbLikes` : Integer
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- ManyToOne avec Utilisateur (auteur)
- ManyToOne avec Deal
- OneToMany avec Commentaire (réponses - self-reference)

---

### 5. **Paiement**
**Champs** :
- `uuid` : UUID (PK)
- `montant` : BigDecimal
- `datePaiement` : LocalDateTime
- `statut` : Enum (EN_ATTENTE, CONFIRME, ECHOUE)
- `methodePaiement` : Enum (CARTE_CREDIT, INTERAC, VIREMENT_BANCAIRE)
- `transactionId` : String
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- ManyToOne avec Utilisateur
- ManyToOne avec Commande

---

### 6. **Commande**
**Champs** :
- `uuid` : UUID (PK)
- `montantTotal` : BigDecimal
- `statut` : Enum (EN_COURS, CONFIRMEE, ANNULEE, REMBOURSEE)
- `dateCommande` : LocalDateTime
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- ManyToOne avec Utilisateur
- ManyToOne avec Deal
- OneToMany avec Paiement

---

### 7. **Adresse**
**Champs** :
- `uuid` : UUID (PK)
- `rue` : String
- `ville` : String
- `codePostal` : String
- `province` : String
- `pays` : String
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- ManyToOne avec Utilisateur

---

### 8. **Notification**
**Champs** :
- `uuid` : UUID (PK)
- `typeNotification` : Enum (INFO, AVERTISSEMENT, ERREUR)
- `message` : String (TEXT)
- `lue` : Boolean
- `dateLecture` : LocalDateTime
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- ManyToOne avec Utilisateur

---

### 9. **ImageDeal**
**Champs** :
- `uuid` : UUID (PK)
- `urlImage` : String (nom fichier avec timestamp)
- `isPrincipal` : Boolean
- `statut` : Enum (PENDING, UPLOADED, FAILED)
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- ManyToOne avec Deal

---

### 10. **ImageUtilisateur**
**Champs** :
- `uuid` : UUID (PK)
- `urlImage` : String (nom fichier avec timestamp)
- `statut` : Enum (PENDING, UPLOADED, FAILED)
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- OneToOne avec Utilisateur

---

### 11. **Publicité**
**Champs** :
- `uuid` : UUID (PK)
- `titre` : String
- `description` : String (TEXT)
- `lienExterne` : String (URL)
- `dateDebut` : LocalDateTime
- `dateFin` : LocalDateTime
- `active` : Boolean
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- OneToMany avec ImagePublicite

**Gestion images** :
- Liste d'images avec statut (PENDING, UPLOADED, FAILED)
- Génération URL présignées pour upload et lecture

---

### 12. **ImagePublicite**
**Champs** :
- `uuid` : UUID (PK)
- `urlImage` : String (nom fichier avec timestamp)
- `statut` : Enum (PENDING, UPLOADED, FAILED)
- `dateCreation` : LocalDateTime
- `dateModification` : LocalDateTime

**Relations** :
- ManyToOne avec Publicité

---

## 🏗️ Structure à créer pour chaque entité

### Module BFF-CORE

#### 1. Modèle (`modele/{Entité}Modele.java`)
```java
package com.ulr.paytogether.core.modele;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {Entité}Modele {
    private UUID uuid;
    // ... champs métier
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
```

#### 2. Interface Provider (`provider/{Entité}Provider.java`)
```java
package com.ulr.paytogether.core.provider;

public interface {Entité}Provider {
    {Entité}Modele sauvegarder({Entité}Modele modele);
    Optional<{Entité}Modele> trouverParUuid(UUID uuid);
    List<{Entité}Modele> trouverTous();
    {Entité}Modele mettreAJour(UUID uuid, {Entité}Modele modele);
    void supprimerParUuid(UUID uuid);
    
    // Si gestion d'images
    void mettreAJourStatutImage(UUID entityUuid, UUID imageUuid, StatutImage statut);
    String obtenirUrlLectureImage(UUID entityUuid, UUID imageUuid);
}
```

#### 3. Interface Service (`domaine/service/{Entité}Service.java`)
```java
package com.ulr.paytogether.core.domaine.service;

public interface {Entité}Service {
    {Entité}Modele creer({Entité}Modele modele);
    Optional<{Entité}Modele> lireParUuid(UUID uuid);
    List<{Entité}Modele> lireTous();
    {Entité}Modele mettreAJour(UUID uuid, {Entité}Modele modele);
    void supprimerParUuid(UUID uuid);
    
    // Si gestion d'images
    void mettreAJourStatutImage(UUID entityUuid, UUID imageUuid, StatutImage statut);
    String obtenirUrlLectureImage(UUID entityUuid, UUID imageUuid);
}
```

#### 4. Implémentation Service (`domaine/impl/{Entité}ServiceImpl.java`)
```java
package com.ulr.paytogether.core.domaine.impl;

@Service
@RequiredArgsConstructor
@Slf4j
public class {Entité}ServiceImpl implements {Entité}Service {
    private final {Entité}Provider provider;
    
    @Override
    public {Entité}Modele creer({Entité}Modele modele) {
        log.info("Création de {entité}: {}", modele);
        // Validation métier si nécessaire
        return provider.sauvegarder(modele);
    }
    
    // ... autres méthodes CRUD
}
```

---

### Module BFF-PROVIDER

#### 1. Entité JPA (`adapter/entity/{Entité}Jpa.java`)
```java
package com.ulr.paytogether.provider.adapter.entity;

@Entity
@Table(name = "{entite}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {Entité}Jpa {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    
    // ... champs
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;
}
```

#### 2. Repository (`repository/{Entité}Repository.java`)
```java
package com.ulr.paytogether.provider.repository;

@Repository
public interface {Entité}Repository extends JpaRepository<{Entité}Jpa, UUID> {
    // Requêtes personnalisées
}
```

**Exemple pour DealRepository** :
```java
@Repository
public interface DealRepository extends JpaRepository<DealJpa, UUID> {
    // Trouver les deals créés par un utilisateur (marchand)
    List<DealJpa> findByMarchandJpaUuid(UUID utilisateurUuid);
    
    // Trouver les deals auxquels un utilisateur participe
    @Query("SELECT d FROM DealJpa d JOIN d.participants p WHERE p.uuid = :utilisateurUuid")
    List<DealJpa> findByParticipantsUuid(@Param("utilisateurUuid") UUID utilisateurUuid);
    
    // Trouver les deals d'une catégorie
    List<DealJpa> findByCategorieJpaUuid(UUID categorieUuid);
    
    // Trouver les deals actifs
    @Query("SELECT d FROM DealJpa d WHERE d.statut = :statut")
    List<DealJpa> findByStatut(@Param("statut") StatutDeal statut);
    
    // Trouver les deals par ville
    List<DealJpa> findByVille(String ville);
}
```

**Exemple pour CategorieRepository** :
```java
@Repository
public interface CategorieRepository extends JpaRepository<CategorieJpa, UUID> {
    // Trouver une catégorie par nom
    Optional<CategorieJpa> findByNom(String nom);
    
    // Vérifier si une catégorie existe par nom
    boolean existsByNom(String nom);
}
```

#### 3. Mapper JPA (`adapter/mapper/{Entité}JpaMapper.java`)
```java
package com.ulr.paytogether.provider.adapter.mapper;

@Component
public class {Entité}JpaMapper {
    public {Entité}Modele versModele({Entité}Jpa jpa) {
        if (jpa == null) return null;
        return {Entité}Modele.builder()
            .uuid(jpa.getUuid())
            // ... mapping
            .build();
    }
    
    public {Entité}Jpa versEntite({Entité}Modele modele) {
        if (modele == null) return null;
        return {Entité}Jpa.builder()
            .uuid(modele.getUuid())
            // ... mapping
            .build();
    }
    
    public void mettreAJour({Entité}Jpa jpa, {Entité}Modele modele) {
        if (jpa == null || modele == null) return;
        // Mise à jour des champs
    }
}
```

**Exemple pour DealJpaMapper** :
```java
@Component
@RequiredArgsConstructor
public class DealJpaMapper {
    private final ImageDealJpaMapper imageDealJpaMapper;
    private final CategorieJpaMapper categorieJpaMapper;
    private final UtilisateurJpaMapper utilisateurJpaMapper;
    
    public DealModele versModele(DealJpa jpa) {
        if (jpa == null) return null;
        
        return DealModele.builder()
            .uuid(jpa.getUuid())
            .titre(jpa.getTitre())
            .description(jpa.getDescription())
            // ... autres champs
            
            // Mapper ManyToOne : marchandJpa -> createur
            .createur(jpa.getMarchandJpa() != null 
                ? utilisateurJpaMapper.versModele(jpa.getMarchandJpa()) 
                : null)
            
            // Mapper ManyToOne : categorieJpa -> categorie
            .categorie(jpa.getCategorieJpa() != null 
                ? categorieJpaMapper.versModele(jpa.getCategorieJpa()) 
                : null)
            
            // Mapper OneToMany : imageDealJpas -> listeImages
            .listeImages(jpa.getImageDealJpas() != null 
                ? jpa.getImageDealJpas().stream()
                    .map(imageDealJpaMapper::versModele)
                    .toList() 
                : null)
            
            // Note: participants (ManyToMany) n'est pas mappé dans le modèle métier
            .build();
    }
    
    public DealJpa versEntite(DealModele modele) {
        if (modele == null) return null;
        
        return DealJpa.builder()
            .uuid(modele.getUuid())
            .titre(modele.getTitre())
            .description(modele.getDescription())
            // ... autres champs
            
            // Mapper createur -> marchandJpa (juste l'UUID)
            .marchandJpa(modele.getCreateur() != null 
                ? UtilisateurJpa.builder()
                    .uuid(modele.getCreateur().getUuid())
                    .build() 
                : null)
            
            // Mapper categorie -> categorieJpa (juste l'UUID)
            .categorieJpa(modele.getCategorie() != null 
                ? CategorieJpa.builder()
                    .uuid(modele.getCategorie().getUuid())
                    .build() 
                : null)
            
            // Mapper listeImages -> imageDealJpas
            .imageDealJpas(modele.getListeImages() != null 
                ? modele.getListeImages().stream()
                    .map(imageDealJpaMapper::versEntite)
                    .toList() 
                : null)
            .build();
    }
}
```

**Important pour les relations** :
- **ManyToOne** : Lors de la conversion Modèle → JPA, ne créer qu'une entité JPA avec l'UUID (pas besoin de tous les champs)
- **OneToMany** : Mapper complètement les collections (images, etc.)
- **ManyToMany** : Les relations `participants` ne sont pas dans le modèle métier, gérées uniquement au niveau JPA

#### 4. Provider Adapter (`adapter/{Entité}ProviderAdapter.java`)
```java
package com.ulr.paytogether.provider.adapter;

@Component
@RequiredArgsConstructor
public class {Entité}ProviderAdapter implements {Entité}Provider {
    private final {Entité}Repository jpaRepository;
    private final {Entité}JpaMapper mapper;
    private final FileManager fileManager; // Si gestion images
    
    @Override
    public {Entité}Modele sauvegarder({Entité}Modele modele) {
        {Entité}Jpa entite = mapper.versEntite(modele);
        
        // Si gestion d'images : ajouter timestamp
        if (modele.getListeImages() != null) {
            // Ajouter timestamp unique
        }
        
        {Entité}Jpa sauvegarde = jpaRepository.save(entite);
        {Entité}Modele resultat = mapper.versModele(sauvegarde);
        
        // Si gestion d'images : générer URL présignées
        setPresignUrl(resultat);
        
        return resultat;
    }
    
    // ... autres méthodes CRUD
    
    // Si gestion d'images
    private void setPresignUrl({Entité}Modele modele) {
        // Générer presignUrl pour images PENDING
    }
}
```

---

### Module BFF-API

#### 1. DTO (`dto/{Entité}DTO.java`)
```java
package com.ulr.paytogether.api.dto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {Entité}DTO {
    private UUID uuid;
    
    @NotBlank(message = "Le champ est obligatoire")
    private String champ;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateCreation;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateModification;
}
```

#### 2. Mapper API (`mapper/{Entité}Mapper.java`)
```java
package com.ulr.paytogether.api.mapper;

@Component
public class {Entité}Mapper {
    public {Entité}DTO modeleVersDto({Entité}Modele modele) {
        if (modele == null) return null;
        return {Entité}DTO.builder()
            .uuid(modele.getUuid())
            // ... mapping
            .build();
    }
    
    public {Entité}Modele dtoVersModele({Entité}DTO dto) {
        if (dto == null) return null;
        return {Entité}Modele.builder()
            .uuid(dto.getUuid())
            // ... mapping
            .build();
    }
}
```

#### 3. Api Adapter (`apiadapter/{Entité}ApiAdapter.java`)
```java
package com.ulr.paytogether.api.apiadapter;

@Component
@RequiredArgsConstructor
public class {Entité}ApiAdapter {
    private final {Entité}Service service;
    private final {Entité}Mapper mapper;
    
    public {Entité}DTO creer({Entité}DTO dto) {
        {Entité}Modele modele = mapper.dtoVersModele(dto);
        {Entité}Modele resultat = service.creer(modele);
        return mapper.modeleVersDto(resultat);
    }
    
    // ... autres méthodes
}
```

#### 4. Resource (`resource/{Entité}Resource.java`)
```java
package com.ulr.paytogether.api.resource;

@RestController
@RequestMapping("/api/{entites}")
@RequiredArgsConstructor
@Slf4j
public class {Entité}Resource {
    private final {Entité}ApiAdapter apiAdapter;
    
    @PostMapping
    public ResponseEntity<{Entité}DTO> creer(@Valid @RequestBody {Entité}DTO dto) {
        log.info("Création de {entité}");
        {Entité}DTO resultat = apiAdapter.creer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultat);
    }
    
    @GetMapping("/{uuid}")
    public ResponseEntity<{Entité}DTO> lireParUuid(@PathVariable UUID uuid) {
        return apiAdapter.trouverParUuid(uuid)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // ... autres endpoints
    
    // Si gestion d'images
    @PatchMapping("/{entityUuid}/images/{imageUuid}/confirm")
    public ResponseEntity<Void> confirmerUploadImage(
        @PathVariable UUID entityUuid,
        @PathVariable UUID imageUuid) {
        // ...
    }
    
    @GetMapping("/{entityUuid}/images/{imageUuid}/url")
    public ResponseEntity<Map<String, String>> obtenirUrlImage(
        @PathVariable UUID entityUuid,
        @PathVariable UUID imageUuid) {
        // ...
    }
}
```

---

## 🧪 Tests à créer

### 1. ServiceImplTest (bff-core/test)
- 10+ tests couvrant tous les cas

### 2. ProviderAdapterTest (bff-provider/test)
- 10+ tests avec mocks

### 3. ApiAdapterTest (bff-api/test)
- 10+ tests

### 4. ResourceTest (bff-api/test)
- 14+ tests d'intégration

---

## 📝 Fichier HTTP (bff-http)

Créer `{entite}.http` avec toutes les requêtes de test.

---

## 🔐 Sécurité

### JWT et WebClient
- Module **bff-wsclient** : clients pour API d'authentification externe
- Configuration WebClient dans une classe dédiée
- Utilisation de tokens JWT pour authentification

---

## ✅ Checklist de validation

Avant de soumettre :
- [ ] Tous les modèles créés dans bff-core
- [ ] Toutes les entités JPA avec suffixe `Jpa`
- [ ] Tous les repositories créés
- [ ] Tous les mappers créés (JPA et API)
- [ ] Tous les services implémentés
- [ ] Tous les endpoints REST fonctionnels
- [ ] Si images : endpoints confirm et url créés
- [ ] Tous les tests unitaires écrits
- [ ] Fichiers HTTP créés
- [ ] Validation Jakarta sur DTOs
- [ ] Format des dates correct
- [ ] Logging ajouté
- [ ] Architecture hexagonale respectée

---

**Date de dernière mise à jour** : 9 février 2026  
**Auteur** : Équipe PayToGether
