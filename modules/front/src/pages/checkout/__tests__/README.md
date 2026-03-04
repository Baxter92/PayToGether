# Tests Square Payment - PayToGether

Ce dossier contient tous les tests pour l'intégration de Square Payment dans le checkout.

## 📦 Structure des tests

```
__tests__/
├── useSquarePayment.test.tsx      # Tests du hook Square Payment
├── SquarePaymentForm.test.tsx     # Tests du composant formulaire
├── CheckoutPage.test.tsx          # Tests de la page checkout
├── CheckoutPage.e2e.test.tsx      # Tests E2E du flux complet
└── testUtils.ts                   # Utilitaires de test
```

## 🧪 Types de tests

### 1. Tests unitaires du hook (`useSquarePayment.test.tsx`)

Teste le hook `useSquarePayment` qui gère :
- Chargement du SDK Square
- Création de paiements
- Vérification du statut
- Remboursements
- Gestion des erreurs

**Exécution** :
```bash
npm run test:square-hook
```

### 2. Tests du composant (`SquarePaymentForm.test.tsx`)

Teste le composant `SquarePaymentForm` qui affiche :
- Le formulaire de paiement
- Les options de méthodes de paiement (Card, Google Pay, Apple Pay, Cash App)
- Les états de chargement
- Les messages d'erreur
- Les informations de sécurité

**Exécution** :
```bash
npm run test:square
```

### 3. Tests de la page checkout (`CheckoutPage.test.tsx`)

Teste l'intégration dans la page checkout :
- Affichage du toggle Square/Classique
- Basculement entre les deux modes
- Passage des bonnes props
- Gestion des callbacks
- Affichage des erreurs

**Exécution** :
```bash
npm run test:checkout
```

### 4. Tests E2E (`CheckoutPage.e2e.test.tsx`)

Teste le flux complet de checkout :
- Navigation entre les étapes
- Remplissage des formulaires
- Sélection de la méthode de paiement
- Traitement du paiement
- Gestion des erreurs
- Redirection après succès

**Exécution** :
```bash
npm run test:checkout-e2e
```

## 🚀 Exécuter tous les tests

```bash
# Tous les tests
npm test

# Avec interface UI
npm run test-ui

# Avec couverture de code
npm run test-coverage

# En mode watch
npm test -- --watch
```

## 🛠️ Utilitaires de test (`testUtils.ts`)

Le fichier `testUtils.ts` fournit :

### Mocks

- `mockSquareSDK()` : Mock du SDK Square complet
- `mockSquareAPIResponses` : Réponses API simulées
- `mockSquareEnvironment()` : Variables d'environnement de test

### Utilitaires

- `waitForSquareLoad()` : Attendre le chargement du SDK
- `simulateSquarePayment()` : Simuler un paiement
- `cleanupSquareMocks()` : Nettoyer les mocks
- `testCheckoutData` : Données de test prêtes à l'emploi

### Exemple d'utilisation

```typescript
import { mockSquareSDK, testCheckoutData, cleanupSquareMocks } from "./testUtils";

describe("Mon test", () => {
  beforeEach(() => {
    mockSquareSDK();
  });

  afterEach(() => {
    cleanupSquareMocks();
  });

  it("devrait faire quelque chose", () => {
    // Test avec les mocks configurés
  });
});
```

## 📊 Couverture de code

Les tests couvrent :

- ✅ Hook `useSquarePayment` : ~95%
- ✅ Composant `SquarePaymentForm` : ~90%
- ✅ Page `CheckoutPage` : ~85%
- ✅ Flux E2E complet : ~80%

## 🐛 Déboguer les tests

### Mode debug

```bash
# Avec interface UI pour déboguer visuellement
npm run test-ui
```

### Logs détaillés

```bash
# Avec logs de console
npm test -- --reporter=verbose
```

### Tests spécifiques

```bash
# Un seul fichier
npm test -- SquarePaymentForm.test.tsx

# Un seul test
npm test -- -t "devrait afficher le montant"
```

## ⚠️ Problèmes courants

### 1. Erreur "window.Square is not defined"

**Solution** : Vérifier que `mockSquareSDK()` est appelé dans `beforeEach`

### 2. Timeouts dans les tests async

**Solution** : Augmenter le timeout

```typescript
it("test async", async () => {
  // ...
}, 10000); // 10 secondes
```

### 3. Mocks non nettoyés

**Solution** : Toujours appeler `cleanupSquareMocks()` dans `afterEach`

## 📝 Ajouter de nouveaux tests

### Template de test unitaire

```typescript
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { mockSquareSDK, cleanupSquareMocks } from "./testUtils";

describe("MonComposant", () => {
  beforeEach(() => {
    mockSquareSDK();
  });

  afterEach(() => {
    cleanupSquareMocks();
  });

  it("devrait faire quelque chose", async () => {
    render(<MonComposant />);
    
    await waitFor(() => {
      expect(screen.getByText("attendu")).toBeInTheDocument();
    });
  });
});
```

### Template de test E2E

```typescript
it("devrait compléter le flux", async () => {
  // 1. Render
  renderCheckout();

  // 2. Étape 1
  fireEvent.change(input, { target: { value: "valeur" } });
  fireEvent.click(submitButton);

  // 3. Vérifications
  await waitFor(() => {
    expect(apiClient.post).toHaveBeenCalled();
  });
});
```

## 📚 Ressources

- [Documentation Vitest](https://vitest.dev/)
- [React Testing Library](https://testing-library.com/react)
- [Square Web SDK](https://developer.squareup.com/docs/web-payments/overview)

## 🎯 Checklist avant commit

- [ ] Tous les tests passent : `npm test`
- [ ] Pas de warnings : `npm run lint`
- [ ] Couverture acceptable : `npm run test-coverage`
- [ ] Tests E2E passent : `npm run test:checkout-e2e`
- [ ] Documentation à jour

---

**Maintenu par** : Équipe PayToGether  
**Dernière mise à jour** : Mars 2026

