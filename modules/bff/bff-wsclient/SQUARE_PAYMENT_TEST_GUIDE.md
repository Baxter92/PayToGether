# Guide de Test - Square Payment Integration

## Prérequis

1. **Compte Square Sandbox**
   - Créer un compte sur https://developer.squareup.com
   - Obtenir les clés API Sandbox

2. **Configuration**

Dans `application.properties` :
```properties
# Square Payment Configuration
square.access-token=YOUR_SANDBOX_ACCESS_TOKEN
square.environment=SANDBOX
square.location-id=YOUR_LOCATION_ID
```

Dans `.env` (frontend) :
```env
VITE_SQUARE_APPLICATION_ID=YOUR_SANDBOX_APPLICATION_ID
VITE_SQUARE_LOCATION_ID=YOUR_LOCATION_ID
```

## Tests Backend

### 1. Compilation

```bash
cd /Users/da/Documents/NewProjet/PayToGether
./mvnw clean compile -pl modules/bff/bff-wsclient
```

**Résultat attendu** : ✅ BUILD SUCCESS

### 2. Test unitaire du SquarePaymentProviderAdapter

Créer un test :

```java
@ExtendWith(MockitoExtension.class)
class SquarePaymentProviderAdapterTest {
    
    @Mock
    private SquareClient squareClient;
    
    @InjectMocks
    private SquarePaymentProviderAdapter adapter;
    
    @Value("${square.location-id}")
    private String locationId;
    
    @Test
    void testCreerPaiement_Success() {
        // Given
        String token = "cnon:card-nonce-ok";
        BigDecimal montant = new BigDecimal("10.00");
        
        // When
        String paymentId = adapter.creerPaiement(token, montant, locationId, "ref-123");
        
        // Then
        assertNotNull(paymentId);
    }
}
```

### 3. Test d'intégration avec Square Sandbox

Utiliser le fichier `modules/bff/bff-http/square-payment.http` :

#### Test 1 : Créer un paiement

```http
POST {{baseUrl}}/api/square-payment
Content-Type: application/json

{
  "commandeUuid": "{{commandeUuid}}",
  "montant": 10.00,
  "squareToken": "cnon:card-nonce-ok",
  "locationId": "{{squareLocationId}}",
  "methodePaiement": "SQUARE_CARD"
}
```

**Cartes de test Square Sandbox** :

| Carte | Résultat |
|-------|----------|
| `cnon:card-nonce-ok` | ✅ Paiement réussi |
| `cnon:card-nonce-declined` | ❌ Paiement refusé |
| `cnon:card-nonce-errors-processing` | ❌ Erreur de traitement |

#### Test 2 : Vérifier le statut

```http
GET {{baseUrl}}/api/square-payment/{{paiementUuid}}/status
```

**Réponse attendue** :
```json
{
  "uuid": "...",
  "statut": "CONFIRME",
  "squarePaymentId": "...",
  "squareReceiptUrl": "https://squareup.com/receipt/..."
}
```

#### Test 3 : Rembourser un paiement

```http
POST {{baseUrl}}/api/square-payment/{{paiementUuid}}/refund
Content-Type: application/json

{
  "montant": 10.00,
  "raison": "Demande client"
}
```

## Tests Frontend

### 1. Composant SquarePaymentForm

Créer une page de test :

```tsx
import { SquarePaymentForm } from '@/components/payment/SquarePaymentForm';

function TestSquarePayment() {
  const handleSuccess = (paymentId: string) => {
    console.log('Paiement réussi:', paymentId);
  };

  const handleError = (error: string) => {
    console.error('Erreur paiement:', error);
  };

  return (
    <div className="container mx-auto p-4">
      <h1>Test Square Payment</h1>
      <SquarePaymentForm
        commandeUuid="test-commande-uuid"
        montant={10.00}
        onSuccess={handleSuccess}
        onError={handleError}
      />
    </div>
  );
}
```

### 2. Test avec cartes de test

Dans le formulaire Square, utiliser :

**Carte de test réussie** :
- Numéro : `4111 1111 1111 1111`
- CVV : `111`
- Code postal : `12345`
- Expiration : toute date future

**Carte de test refusée** :
- Numéro : `4000 0000 0000 0002`

## Vérifications

### ✅ Checklist Backend

- [ ] Compilation sans erreur
- [ ] SquareClient bean créé correctement
- [ ] Logs montrent la création du paiement
- [ ] ID de paiement retourné
- [ ] Statut mis à jour en base de données
- [ ] URL de reçu générée

### ✅ Checklist Frontend

- [ ] Web Payment SDK chargé
- [ ] Formulaire de carte affiché
- [ ] Tokenisation réussie
- [ ] Appel API backend réussi
- [ ] Message de succès affiché
- [ ] Redirection après paiement

## Erreurs courantes

### 1. "Cannot resolve method 'getPaymentsApi'"

**Cause** : Mauvaise version du SDK ou imports incorrects  
**Solution** : Vérifier que la version 46.0.0.20260122 est bien utilisée

### 2. "ApiException: Unauthorized"

**Cause** : Token d'accès invalide  
**Solution** : Vérifier `square.access-token` dans application.properties

### 3. "Location not found"

**Cause** : Location ID invalide  
**Solution** : Vérifier `square.location-id` depuis le Dashboard Square

### 4. "Payment declined"

**Cause** : Carte de test refusée ou montant invalide  
**Solution** : Utiliser `cnon:card-nonce-ok` pour les tests

## Logs à vérifier

```
INFO  SquarePaymentProviderAdapter - Creating Square payment: amount=10.00, locationId=..., referenceId=...
INFO  SquarePaymentProviderAdapter - Square payment created successfully: paymentId=...
INFO  SquarePaymentServiceImpl - Paiement Square créé avec UUID: ...
```

## Monitoring

En production, surveiller :

1. **Taux de réussite des paiements** : doit être > 95%
2. **Temps de réponse** : < 3 secondes
3. **Erreurs Square API** : < 1%
4. **Webhooks Square** : réception des événements

## Support

- **Documentation Square** : https://developer.squareup.com/docs
- **Status Page** : https://status.squareup.com
- **Dashboard** : https://squareup.com/dashboard

---

**Dernière mise à jour** : 4 mars 2026

