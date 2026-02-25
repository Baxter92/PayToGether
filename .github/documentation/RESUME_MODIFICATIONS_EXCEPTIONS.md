# Résumé des modifications - Système d'exceptions traduisibles

## 📅 Date : 25 février 2026

---

## ✅ Ce qui a été créé

### 1. **Exceptions métier personnalisées** (bff-core/exception/)

#### BusinessException (base)
- `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/exception/BusinessException.java`
- Exception de base avec code d'erreur traduisible et paramètres
- Constructeurs multiples pour différents cas d'usage

#### Exceptions spécialisées

1. **ValidationException**
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/exception/ValidationException.java`
   - Pour les erreurs de validation métier
   - HTTP 400 - Bad Request

2. **ResourceNotFoundException**
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/exception/ResourceNotFoundException.java`
   - Pour les ressources non trouvées
   - HTTP 404 - Not Found
   - Méthode pratique : `ResourceNotFoundException.parUuid("deal", uuid)`

3. **DuplicateResourceException**
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/exception/DuplicateResourceException.java`
   - Pour les duplications (ex: email existant)
   - HTTP 409 - Conflict
   - Méthode pratique : `DuplicateResourceException.emailExistant(email)`

4. **ForbiddenOperationException**
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/exception/ForbiddenOperationException.java`
   - Pour les opérations interdites selon règles métier
   - HTTP 403 - Forbidden

5. **FileStorageException**
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/exception/FileStorageException.java`
   - Pour les erreurs MinIO/stockage
   - HTTP 500 - Internal Server Error

---

### 2. **Validators complets** (bff-core/domaine/validator/)

#### Validators mis à jour avec exceptions traduisibles

1. **DealValidator** ✅
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/domaine/validator/DealValidator.java`
   - Méthodes : `valider()`, `validerPourMiseAJour()`
   - Toutes les règles métier pour les deals

2. **UtilisateurValidator** ✅
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/domaine/validator/UtilisateurValidator.java`
   - Méthodes : `valider()`, `validerPourCreation()`, `validerPourMiseAJour()`
   - Validation email, mot de passe, etc.

#### Nouveaux Validators créés

3. **CategorieValidator** ✅
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/domaine/validator/CategorieValidator.java`
   - Validation du nom, longueur max

4. **PubliciteValidator** ✅
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/domaine/validator/PubliciteValidator.java`
   - Validation titre, description, dates, activation/désactivation

5. **CommandeValidator** ✅
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/domaine/validator/CommandeValidator.java`
   - Validation montant, statut, annulation, confirmation, paiement

6. **CommentaireValidator** ✅
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/domaine/validator/CommentaireValidator.java`
   - Validation contenu, longueur min/max

7. **AdresseValidator** ✅
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/domaine/validator/AdresseValidator.java`
   - Validation code postal canadien, rue, ville, province

8. **PaiementValidator** ✅
   - `/modules/bff/bff-core/src/main/java/com/ulr/paytogether/core/domaine/validator/PaiementValidator.java`
   - Validation montant, méthode, remboursement, confirmation

---

### 3. **Gestion des erreurs côté API** (bff-api/)

1. **ErrorResponseDTO**
   - `/modules/bff/bff-api/src/main/java/com/ulr/paytogether/api/dto/ErrorResponseDTO.java`
   - DTO pour les réponses d'erreur
   - Contient : errorCode, params, status, timestamp

2. **GlobalExceptionHandler**
   - `/modules/bff/bff-api/src/main/java/com/ulr/paytogether/api/exception/GlobalExceptionHandler.java`
   - Intercepte toutes les exceptions
   - Transforme en réponses HTTP avec codes traduisibles
   - Gère : ValidationException, ResourceNotFoundException, DuplicateResourceException, ForbiddenOperationException, FileStorageException

---

### 4. **Documentation**

1. **CODES_ERREUR_TRADUISIBLES.md** ✅
   - `/.github/documentation/CODES_ERREUR_TRADUISIBLES.md`
   - Liste complète de ~80+ codes d'erreur
   - Format des codes : `{entité}.{attribut}.{type}`
   - Exemples de traduction FR/EN
   - Guide d'utilisation backend/frontend

2. **Mise à jour copilot-instructions.md** ✅
   - `/.github/copilot-instructions.md`
   - Nouvelle section complète sur Validators et Exceptions
   - Règles d'or mises à jour
   - Checklist CRUD mise à jour
   - Exemples de code

---

## 📊 Statistiques

- **Exceptions créées** : 6 (1 base + 5 spécialisées)
- **Validators créés/mis à jour** : 8
- **Codes d'erreur documentés** : ~80+
- **Entités couvertes** : 8 (Deal, Utilisateur, Catégorie, Publicité, Commande, Commentaire, Adresse, Paiement)

---

## 🎯 Principes clés

### 1. Toutes les règles métier dans les Validators
```java
@Component
public class DealValidator {
    public void valider(DealModele deal) {
        if (deal.getTitre() == null || deal.getTitre().isBlank()) {
            throw new ValidationException("deal.titre.obligatoire");
        }
    }
}
```

### 2. Format des codes d'erreur : `{entité}.{attribut}.{type}`
```java
throw new ValidationException("deal.titre.obligatoire");
throw new ValidationException("deal.description.longueur", 5000);
throw ResourceNotFoundException.parUuid("deal", dealUuid);
```

### 3. Validation obligatoire dans les Services
```java
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {
    private final DealValidator dealValidator;
    
    public DealModele creer(DealModele deal) {
        dealValidator.valider(deal);  // ✅ OBLIGATOIRE
        return dealProvider.sauvegarder(deal);
    }
}
```

### 4. Traduction côté Frontend
**Réponse API :**
```json
{
  "errorCode": "deal.description.longueur",
  "params": [5000],
  "status": 400
}
```

**Traduction FR :**
```json
{
  "errors": {
    "deal": {
      "description": {
        "longueur": "La description ne peut pas dépasser {{0}} caractères"
      }
    }
  }
}
```

---

## 🚀 Prochaines étapes

### Pour utiliser le système d'exceptions :

1. **Dans les Services existants** :
   - Remplacer `IllegalArgumentException` par `ValidationException`
   - Ajouter les appels aux validators

2. **Pour créer une nouvelle entité** :
   - Créer le Validator avec toutes les règles métier
   - Utiliser uniquement `ValidationException` avec codes traduisibles
   - Documenter les codes d'erreur dans `CODES_ERREUR_TRADUISIBLES.md`

3. **Côté Frontend** :
   - Créer les fichiers de traduction (fr.json, en.json)
   - Utiliser les codes d'erreur pour afficher les messages

---

## 📝 Exemple complet

### Backend - Validator
```java
@Component
public class DealValidator {
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    
    public void valider(DealModele deal) {
        if (deal == null) {
            throw new ValidationException("deal.null");
        }
        if (deal.getTitre() == null || deal.getTitre().isBlank()) {
            throw new ValidationException("deal.titre.obligatoire");
        }
        if (deal.getDescription() != null && 
            deal.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("deal.description.longueur", MAX_DESCRIPTION_LENGTH);
        }
    }
}
```

### Backend - Service
```java
@Service
@RequiredArgsConstructor
public class DealServiceImpl implements DealService {
    private final DealProvider dealProvider;
    private final DealValidator dealValidator;
    
    @Override
    public DealModele creer(DealModele deal) {
        dealValidator.valider(deal);
        return dealProvider.sauvegarder(deal);
    }
}
```

### Frontend - Traduction
```typescript
// fr.json
{
  "errors": {
    "deal": {
      "null": "Le deal ne peut pas être vide",
      "titre": {
        "obligatoire": "Le titre est obligatoire"
      },
      "description": {
        "longueur": "La description ne peut pas dépasser {{0}} caractères"
      }
    }
  }
}
```

### Frontend - Utilisation
```typescript
const { mutate } = useMutation({
  onError: (error: BusinessError) => {
    const message = translateError(error.errorCode, error.params);
    toast.error(message);
  }
});
```

---

## ✅ Vérification

- [x] Exceptions créées dans bff-core
- [x] Validators créés/mis à jour pour toutes les entités
- [x] GlobalExceptionHandler créé dans bff-api
- [x] ErrorResponseDTO créé
- [x] Documentation complète des codes d'erreur
- [x] copilot-instructions.md mis à jour
- [x] Exemples de code fournis

---

**Système d'exceptions traduisibles : COMPLÉTÉ ✅**

Toutes les règles métier sont maintenant centralisées dans les Validators et utilisent des codes d'erreur traduisibles côté frontend.

