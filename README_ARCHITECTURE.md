# 🏗️ Architecture Hexagonale - PayToGether

[![Architecture](https://img.shields.io/badge/Architecture-Hexagonale-blue)]()
[![DDD](https://img.shields.io/badge/Pattern-DDD-green)]()
[![Conformité](https://img.shields.io/badge/Conformit%C3%A9-100%25-brightgreen)]()

## 📚 Documentation Complète

🎯 **Commencez ici :** [INDEX.md](INDEX.md)

| Document | Description |
|----------|-------------|
| **[INDEX.md](INDEX.md)** | 📖 Point d'entrée et navigation |
| **[ARCHITECTURE_FINAL_SUMMARY.md](ARCHITECTURE_FINAL_SUMMARY.md)** | 📊 Résumé complet avec statistiques |
| **[ARCHITECTURE_HEXAGONALE_CONFORME.md](ARCHITECTURE_HEXAGONALE_CONFORME.md)** | 📘 Architecture détaillée |
| **[ARCHITECTURE_COMPLETE.md](ARCHITECTURE_COMPLETE.md)** | 📙 Vue d'ensemble |

## 🎯 Résumé Rapide

### Architecture Implémentée

✅ **Architecture Hexagonale** stricte  
✅ **Domain-Driven Design (DDD)**  
✅ **Séparation des responsabilités** claire  
✅ **Nomenclature cohérente** en français camelCase  
✅ **36+ fichiers créés** suivant le pattern établi  

### Structure

```
bff-core/          → Domaine Métier (Modèles purs + Ports)
bff-provider/      → Infrastructure (JPA + Adapters)
bff-api/           → Points d'entrée HTTP (REST + DTOs)
bff-configuration/ → Configuration Spring
bff-wsclient/      → Clients WebService
```

## 🔑 Nomenclature

### Entités de Base de Données (bff-provider)
```
⭐ Préfixe "Jpa" obligatoire !

JpaUtilisateur.java
JpaDeal.java
JpaCategorie.java
etc.
```

### Modèles Métier (bff-core)
```
Sans annotations JPA !

UtilisateurModele.java
DealModele.java
CategorieModele.java
etc.
```

### Ports (bff-core)
```
Interfaces pour les adapters

UtilisateurPort.java
DealPort.java
etc.
```

## 📊 Entités Implémentées

| Entité | Core | Provider | API | Statut |
|--------|------|----------|-----|--------|
| Utilisateur | ✅ | ✅ | ✅ | 🟢 Complet |
| Deal | ✅ | ✅ | 🟡 | 🟡 Partiel |
| Catégorie | ✅ | ✅ | ⏳ | 🟡 À compléter |
| Commentaire | ✅ | ✅ | ⏳ | 🟡 À compléter |
| Paiement | ✅ | ✅ | ⏳ | 🟡 À compléter |
| Commande | ✅ | ✅ | ⏳ | 🟡 À compléter |
| Adresse | ✅ | ✅ | ⏳ | 🟡 À compléter |

## 🚀 Quick Start

### Voir le résumé visuel
```bash
bash RESUME_RAPIDE.sh
```

### Vérifier l'architecture
```bash
bash verify-complete-architecture.sh
```

### Suivre le pattern (Exemple : Utilisateur)

**1. Modèle Core (sans JPA)**
```
bff-core/src/main/java/com/ulr/paytogether/core/modele/UtilisateurModele.java
```

**2. Port (interface)**
```
bff-core/src/main/java/com/ulr/paytogether/core/port/UtilisateurPort.java
```

**3. Entité JPA (avec préfixe "Jpa")**
```
bff-provider/src/main/java/com/ulr/paytogether/provider/adapter/entity/JpaUtilisateur.java
```

**4. Adapter (implémente le Port)**
```
bff-provider/src/main/java/com/ulr/paytogether/provider/adapter/UtilisateurJpaAdapter.java
```

## 🔄 Flux de Données

```
HTTP Request
    ↓
UtilisateurResource (bff-api)
    ↓
UtilisateurApiAdapter (bff-api)
    ↓
UtilisateurMapper: DTO → UtilisateurModele
    ↓
UtilisateurServiceCore (bff-core)
    ↓
UtilisateurPort (interface)
    ↓
UtilisateurJpaAdapter (bff-provider)
    ↓
UtilisateurJpaMapper: UtilisateurModele → JpaUtilisateur
    ↓
JpaUtilisateurRepository
    ↓
PostgreSQL
```

## 📋 Checklist pour Nouvelle Entité

Suivez ce pattern pour chaque nouvelle entité :

### BFF-CORE
- [ ] Créer `*Modele.java` (modèle pur)
- [ ] Créer `*Port.java` (interface)
- [ ] Créer `*ServiceCore.java` (service métier)

### BFF-PROVIDER
- [ ] Créer `Jpa*.java` (entité avec @Entity)
- [ ] Créer `Jpa*Repository.java` (Spring Data)
- [ ] Créer `*JpaMapper.java` (JPA ↔ Core)
- [ ] Créer `*JpaAdapter.java` (implémente Port)

### BFF-API
- [ ] Créer `*DTO.java` et `Creer*DTO.java`
- [ ] Créer `*Mapper.java` (DTO ↔ Core)
- [ ] Créer `*ApiAdapter.java` (Resource → Core)
- [ ] Créer `*Resource.java` (Controller REST)

## ✅ Conformité aux Instructions

Conforme à 100% au fichier `.github/copilot-instructions.md` :

- ✅ Package `adapter` dans bff-provider
- ✅ Package `mapper` pour JPA
- ✅ Préfixe "Jpa" pour entités BD
- ✅ Modèles Core sans JPA
- ✅ Services utilisent Ports
- ✅ Package `apiadapter` dans bff-api
- ✅ Mapping Modèle Core → DTO
- ✅ Variables en français camelCase

## 🎓 Avantages de cette Architecture

### 1. Indépendance Technologique
- Core ne dépend pas de JPA
- Changement de BD facile
- Tests sans base de données

### 2. Testabilité
```java
// Test unitaire pur
UtilisateurPort mockPort = mock(UtilisateurPort.class);
UtilisateurServiceCore service = new UtilisateurServiceCore(mockPort);
```

### 3. Maintenabilité
- Changement d'une couche = Impact isolé
- Code organisé et navigable
- Pattern clair et répétable

### 4. Scalabilité
- Ajout d'entités simple
- Ajout de providers transparent
- Ajout d'APIs facile

## 📖 Pour en Savoir Plus

- **Architecture Hexagonale :** Alistair Cockburn
- **Domain-Driven Design :** Eric Evans
- **Clean Architecture :** Robert C. Martin

## 📊 Statistiques

- **Fichiers créés :** 36+
- **Entités traitées :** 7
- **Conformité :** 100%
- **Date :** 26 janvier 2026

## 🎉 Conclusion

**Base solide établie !**

L'architecture hexagonale est en place avec :
- Pattern répétable
- Exemple complet (Utilisateur)
- Documentation exhaustive
- Scripts de vérification

**Il suffit de suivre le pattern pour compléter ! 🚀**

---

*Pour plus de détails, consultez [INDEX.md](INDEX.md)*
