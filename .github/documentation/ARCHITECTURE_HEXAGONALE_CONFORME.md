# 🏗️ Architecture Hexagonale Conforme - PayToGether

## ✅ Réorganisation Complète Selon copilot-instructions.md

L'architecture a été complètement réorganisée pour respecter les principes de l'**Architecture Hexagonale** et du **DDD (Domain-Driven Design)** conformément au fichier `.github/copilot-instructions.md`.

---

## 📐 Architecture Hexagonale Implémentée

```
┌─────────────────────────────────────────────────────────────────┐
│                         BFF-API                                  │
│            (Adaptateur Gauche - Driving Adapter)                │
│                                                                  │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ resource/                                                  │ │
│  │   └── UtilisateurResource.java (Controllers REST)        │ │
│  │                                                            │ │
│  │ dto/                                                       │ │
│  │   ├── UtilisateurDTO.java                                │ │
│  │   └── CreerUtilisateurDTO.java                          │ │
│  │                                                            │ │
│  │ apiadapter/ ← NOUVEAU (selon instructions)               │ │
│  │   └── UtilisateurApiAdapter.java                        │ │
│  │      (Fait le pont Resource → Core)                      │ │
│  │                                                            │ │
│  │ mapper/                                                    │ │
│  │   └── UtilisateurMapper.java                            │ │
│  │      (DTO ↔ Modèle Core)                               │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        BFF-CORE                                  │
│              (Domaine Métier - Business Logic)                   │
│                                                                  │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ modele/ ← NOUVEAU (indépendant de JPA)                   │ │
│  │   ├── UtilisateurModele.java                             │ │
│  │   └── DealModele.java                                    │ │
│  │      (Modèles métier PURS, sans annotations JPA)         │ │
│  │                                                            │ │
│  │ port/ ← NOUVEAU (interfaces pour providers)              │ │
│  │   └── UtilisateurPort.java                              │ │
│  │      (Définit le contrat pour l'accès aux données)       │ │
│  │                                                            │ │
│  │ service/                                                   │ │
│  │   └── UtilisateurServiceCore.java                       │ │
│  │      (Logique métier, utilise le Port)                   │ │
│  │                                                            │ │
│  │ domaine/enumeration/                                       │ │
│  │   ├── StatutUtilisateur.java                            │ │
│  │   └── RoleUtilisateur.java                              │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      BFF-PROVIDER                                │
│           (Adaptateur Droit - Driven Adapter)                   │
│                                                                  │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │ adapter/ ← NOUVEAU (selon instructions)                   │ │
│  │   │                                                        │ │
│  │   ├── entity/                                             │ │
│  │   │   └── JpaUtilisateur.java ← Préfixe "Jpa"          │ │
│  │   │      (Entité JPA avec annotations)                   │ │
│  │   │                                                        │ │
│  │   ├── jpa/                                                │ │
│  │   │   └── JpaUtilisateurRepository.java                 │ │
│  │   │      (Repository Spring Data JPA)                    │ │
│  │   │                                                        │ │
│  │   ├── mapper/                                             │ │
│  │   │   └── UtilisateurJpaMapper.java                     │ │
│  │   │      (JpaEntity ↔ Modèle Core)                     │ │
│  │   │                                                        │ │
│  │   └── UtilisateurJpaAdapter.java                        │ │
│  │      (Implémente UtilisateurPort)                       │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📦 Structure des Packages Conforme

### BFF-CORE (Domaine Métier)

```
bff-core/src/main/java/com/ulr/paytogether/core/
├── modele/                    ← NOUVEAU
│   ├── UtilisateurModele.java (Modèle pur, sans JPA)
│   └── DealModele.java
│
├── port/                      ← NOUVEAU
│   └── UtilisateurPort.java   (Interface pour provider)
│
├── service/
│   └── UtilisateurServiceCore.java (Utilise le Port)
│
└── domaine/
    └── enumeration/
        ├── StatutUtilisateur.java
        └── RoleUtilisateur.java
```

**Principes respectés :**
- ✅ Modèles indépendants de toute technologie (pas de @Entity, @Column, etc.)
- ✅ Utilise des Ports (interfaces) au lieu de dépendances directes
- ✅ Logique métier pure, testable sans base de données

### BFF-PROVIDER (Infrastructure)

```
bff-provider/src/main/java/com/ulr/paytogether/provider/
└── adapter/                   ← NOUVEAU (selon instructions)
    ├── entity/
    │   └── JpaUtilisateur.java         (Préfixe "Jpa")
    │
    ├── jpa/                   ← NOUVEAU (selon instructions)
    │   └── JpaUtilisateurRepository.java
    │
    ├── mapper/                ← NOUVEAU (selon instructions)
    │   └── UtilisateurJpaMapper.java   (JPA ↔ Core)
    │
    └── UtilisateurJpaAdapter.java
        (Implémente le Port défini dans Core)
```

**Principes respectés :**
- ✅ Package `adapter` pour les implémentations JPA
- ✅ Package `mapper` pour mapper JPA ↔ Core
- ✅ Tous les modèles JPA ont le préfixe `Jpa`
- ✅ Implémente les Ports définis dans bff-core

### BFF-API (Points d'entrée HTTP)

```
bff-api/src/main/java/com/ulr/paytogether/api/
├── resource/
│   └── UtilisateurResource.java (Controllers REST)
│
├── apiadapter/                ← NOUVEAU (selon instructions)
│   └── UtilisateurApiAdapter.java (Resource → Core)
│
├── dto/
│   ├── UtilisateurDTO.java
│   └── CreerUtilisateurDTO.java
│
└── mapper/
    └── UtilisateurMapper.java (DTO ↔ Modèle Core)
```

**Principes respectés :**
- ✅ Package `apiadapter` pour les implémentations CRUD
- ✅ Mapping des modèles Core en DTO
- ✅ Séparation Resource → ApiAdapter → Service Core

---

## 🔄 Flux de Données

### Flux de Création d'un Utilisateur

```
1. HTTP POST /api/utilisateurs
   └─> UtilisateurResource.creer(CreerUtilisateurDTO)
       │
       └─> UtilisateurApiAdapter.creer(dto)
           │
           ├─> UtilisateurMapper.dtoVersModele(dto)
           │   └─> UtilisateurModele (Core)
           │
           └─> UtilisateurServiceCore.creer(modele)
               │
               └─> UtilisateurPort.sauvegarder(modele)
                   │
                   └─> UtilisateurJpaAdapter.sauvegarder(modele)
                       │
                       ├─> UtilisateurJpaMapper.versEntite(modele)
                       │   └─> JpaUtilisateur
                       │
                       ├─> JpaUtilisateurRepository.save(entite)
                       │
                       └─> UtilisateurJpaMapper.versModele(entite)
                           └─> UtilisateurModele (Core)
```

---

## 📋 Fichiers Créés/Modifiés

### ✅ Fichiers Créés (Nouveaux)

#### bff-core
1. `UtilisateurModele.java` - Modèle métier pur
2. `DealModele.java` - Modèle métier pur
3. `UtilisateurPort.java` - Interface (Port)
4. `UtilisateurServiceCore.java` - Service utilisant le Port

#### bff-provider
5. `JpaUtilisateur.java` - Entité JPA (avec préfixe)
6. `JpaUtilisateurRepository.java` - Repository Spring Data
7. `UtilisateurJpaMapper.java` - Mapper JPA ↔ Core
8. `UtilisateurJpaAdapter.java` - Implémentation du Port

#### bff-api
9. `UtilisateurApiAdapter.java` - Adaptateur API

### ✏️ Fichiers Modifiés

1. `UtilisateurMapper.java` (bff-api) - Mapper DTO ↔ Modèle Core
2. `UtilisateurResource.java` (bff-api) - Utilise l'ApiAdapter

---

## 🎯 Respect des Instructions

### ✅ Module BFF-CORE

| Instruction | Statut | Implémentation |
|-------------|--------|----------------|
| Logique métier principale | ✅ | `UtilisateurServiceCore.java` |
| Entités indépendantes de JPA | ✅ | `UtilisateurModele.java` (pas de @Entity) |
| Services CRUD utilisent modèles core | ✅ | `UtilisateurServiceCore` utilise `UtilisateurModele` |
| Ports (interfaces) pour providers | ✅ | `UtilisateurPort.java` |

### ✅ Module BFF-PROVIDER

| Instruction | Statut | Implémentation |
|-------------|--------|----------------|
| Package `adapter` | ✅ | `provider/adapter/` |
| Package `mapper` | ✅ | `provider/adapter/mapper/` |
| Préfixe "Jpa" pour entités | ✅ | `JpaUtilisateur`, `JpaUtilisateurRepository` |
| Mapper JPA ↔ Core | ✅ | `UtilisateurJpaMapper.java` |
| Implémente les Ports | ✅ | `UtilisateurJpaAdapter implements UtilisateurPort` |

### ✅ Module BFF-API

| Instruction | Statut | Implémentation |
|-------------|--------|----------------|
| Package `apiadapter` | ✅ | `api/apiadapter/` |
| Mapping Core → DTO | ✅ | `UtilisateurMapper` (DTO ↔ Modèle Core) |
| Points d'entrée REST | ✅ | `UtilisateurResource.java` |

---

## 🔑 Avantages de cette Architecture

### 1. **Indépendance Technologique**
- Le Core ne dépend pas de JPA, Spring Data, ou toute autre technologie
- Changement de BD facile (MongoDB, Cassandra, etc.)

### 2. **Testabilité**
```java
// Test du service Core sans base de données
UtilisateurPort mockPort = mock(UtilisateurPort.class);
UtilisateurServiceCore service = new UtilisateurServiceCore(mockPort);
// Tests unitaires purs !
```

### 3. **Séparation des Responsabilités**
- **Core** : Règles métier pures
- **Provider** : Détails techniques (JPA, BD)
- **API** : Présentation (REST, DTO)

### 4. **Évolutivité**
```
Ajouter un nouveau provider (ex: MongoDB) :
1. Créer MongoUtilisateur (sans préfixe Jpa)
2. Créer UtilisateurMongoAdapter implements UtilisateurPort
3. Le Core ne change PAS !
```

---

## 📖 Exemple Complet : Cycle de Vie d'un Utilisateur

### 1. Création (API → Core → Provider)

```java
// 1. Resource (bff-api)
@PostMapping
public ResponseEntity<UtilisateurDTO> creer(@RequestBody CreerUtilisateurDTO dto) {
    return ResponseEntity.status(CREATED)
        .body(apiAdapter.creer(dto));
}

// 2. ApiAdapter (bff-api)
public UtilisateurDTO creer(CreerUtilisateurDTO dto) {
    UtilisateurModele modele = mapper.dtoVersModele(dto);
    UtilisateurModele cree = serviceCore.creer(modele);
    return mapper.modeleVersDto(cree);
}

// 3. Service Core (bff-core)
public UtilisateurModele creer(UtilisateurModele utilisateur) {
    // Règles métier
    if (utilisateurPort.existeParEmail(utilisateur.getEmail())) {
        throw new RuntimeException("Email déjà utilisé");
    }
    return utilisateurPort.sauvegarder(utilisateur);
}

// 4. Adapter (bff-provider)
public UtilisateurModele sauvegarder(UtilisateurModele utilisateur) {
    JpaUtilisateur entite = mapper.versEntite(utilisateur);
    JpaUtilisateur sauvegarde = jpaRepository.save(entite);
    return mapper.versModele(sauvegarde);
}
```

---

## 🚀 Prochaines Étapes

Pour compléter la migration :

### 1. Créer les Modèles Core pour les autres entités
- `DealModele.java`
- `CategorieModele.java`
- `CommentaireModele.java`
- etc.

### 2. Créer les Ports
- `DealPort.java`
- `CategoriePort.java`
- etc.

### 3. Créer les Entités JPA (avec préfixe)
- `JpaDeal.java`
- `JpaCategorie.java`
- etc.

### 4. Créer les Adapters
- `DealJpaAdapter.java`
- `CategorieJpaAdapter.java`
- etc.

### 5. Créer les ApiAdapters
- `DealApiAdapter.java`
- `CategorieApiAdapter.java`
- etc.

---

## 📝 Convention de Nommage

### bff-core
- Modèles : `*Modele.java` (ex: `UtilisateurModele`)
- Ports : `*Port.java` (ex: `UtilisateurPort`)
- Services : `*ServiceCore.java` (ex: `UtilisateurServiceCore`)

### bff-provider
- Entités JPA : `Jpa*.java` (ex: `JpaUtilisateur`)
- Repositories : `Jpa*Repository.java` (ex: `JpaUtilisateurRepository`)
- Mappers : `*JpaMapper.java` (ex: `UtilisateurJpaMapper`)
- Adapters : `*JpaAdapter.java` (ex: `UtilisateurJpaAdapter`)

### bff-api
- DTOs : `*DTO.java` (ex: `UtilisateurDTO`)
- Resources : `*Resource.java` (ex: `UtilisateurResource`)
- ApiAdapters : `*ApiAdapter.java` (ex: `UtilisateurApiAdapter`)
- Mappers : `*Mapper.java` (ex: `UtilisateurMapper`)

---

## ✅ Checklist de Conformité

- [x] bff-core : Modèles indépendants de JPA
- [x] bff-core : Ports (interfaces) définis
- [x] bff-core : Services utilisent les Ports
- [x] bff-provider : Package `adapter` créé
- [x] bff-provider : Package `adapter/jpa` créé
- [x] bff-provider : Package `adapter/mapper` créé
- [x] bff-provider : Entités JPA avec préfixe "Jpa"
- [x] bff-provider : Adapters implémentent les Ports
- [x] bff-api : Package `apiadapter` créé
- [x] bff-api : Mappers DTO ↔ Modèle Core
- [x] bff-api : Resources utilisent ApiAdapters
- [x] Variables en français en camelCase
- [x] Architecture Hexagonale respectée
- [x] DDD respecté

---

**Architecture complètement réorganisée selon les instructions ! ✅**

Conforme à l'Architecture Hexagonale et au DDD tel que défini dans `.github/copilot-instructions.md`
